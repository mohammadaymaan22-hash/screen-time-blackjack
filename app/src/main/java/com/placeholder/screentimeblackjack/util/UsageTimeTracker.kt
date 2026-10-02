package com.placeholder.screentimeblackjack.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.placeholder.screentimeblackjack.data.AppDatabase
import com.placeholder.screentimeblackjack.data.BlockedApp
import com.placeholder.screentimeblackjack.service.MonitorForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton managing independent second-by-second screen time tracking for each monitored app.
 *
 * Rules:
 * 1. Each app has its own independent countdown timer (e.g., YouTube: 15m, Instagram: 5m).
 * 2. Only the app currently in active foreground use has its time decremented.
 * 3. Keyboards, volume sliders, and transient system notifications do NOT pause or cancel tracking.
 * 4. When an app is out of time (0s) and launched, it kicks the user back to HOME and shows a toast.
 *    It does NOT forcefully launch Screen Time Casino.
 * 5. Screen OFF or switching to Home/other apps immediately pauses countdown.
 */
object UsageTimeTracker {

    private const val TAG = "UsageTimeTracker"
    private const val PREFS_NAME = "per_app_screen_time_prefs"

    private val trackerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null

    /** Currently active foreground package (if monitored) */
    private val _activeMonitoredPackage = MutableStateFlow<String?>(null)
    val activeMonitoredPackage: StateFlow<String?> = _activeMonitoredPackage.asStateFlow()

    /** Human-readable name of currently active monitored app */
    private val _activeAppName = MutableStateFlow<String?>(null)
    val activeAppName: StateFlow<String?> = _activeAppName.asStateFlow()

    /** Seconds remaining for the currently active monitored app */
    private val _activeAppSeconds = MutableStateFlow(0)
    val activeAppSeconds: StateFlow<Int> = _activeAppSeconds.asStateFlow()

    /** Map of packageName -> remaining seconds for all monitored apps */
    private val _appSecondsMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val appSecondsMap: StateFlow<Map<String, Int>> = _appSecondsMap.asStateFlow()

    /** True if any monitored app is actively ticking down */
    private val _isActivelyTracking = MutableStateFlow(false)
    val isActivelyTracking: StateFlow<Boolean> = _isActivelyTracking.asStateFlow()

    @Volatile
    private var isScreenInteractive: Boolean = true

    private val isInitialized = java.util.concurrent.atomic.AtomicBoolean(false)
    private val tickerLock = Any()

    /** Callback invoked by AccessibilityService when an app is blocked (kick to home) */
    @Volatile
    var blockCallback: ((packageName: String, appName: String) -> Unit)? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        if (!isInitialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext

        trackerScope.launch {
            val db = AppDatabase.getInstance(appContext)
            val apps = db.blockedAppDao().getActiveBlockedApps()
            val prefs = getPrefs(appContext)

            val initialMap = mutableMapOf<String, Int>()
            apps.forEach { app ->
                val savedSec = prefs.getInt("sec_${app.packageName}", -1)
                val sec = if (savedSec >= 0) savedSec else app.timeBalanceMinutes * 60
                initialMap[app.packageName] = sec
                prefs.edit().putInt("sec_${app.packageName}", sec).apply()
            }

            _appSecondsMap.value = initialMap
            Log.d(TAG, "UsageTimeTracker initialized with ${initialMap.size} monitored apps")

            startTickerLoop(appContext)
        }
    }

    private fun startTickerLoop(context: Context) {
        synchronized(tickerLock) {
            tickerJob?.cancel()
            tickerJob = trackerScope.launch {
                var lastActiveMs = 0L

                while (isActive) {
                    delay(500L)

                    val activePkg = _activeMonitoredPackage.value
                    val isTrackingNow = activePkg != null && isScreenInteractive
                    _isActivelyTracking.value = isTrackingNow

                    val now = android.os.SystemClock.elapsedRealtime()

                    if (isTrackingNow) {
                        if (lastActiveMs == 0L) {
                            lastActiveMs = now
                        } else {
                            val elapsedMs = now - lastActiveMs
                            val elapsedSeconds = (elapsedMs / 1000L).toInt()

                            if (elapsedSeconds >= 1) {
                                lastActiveMs += elapsedSeconds * 1000L

                                val currentSec = getAppSeconds(context, activePkg!!)
                                if (currentSec <= elapsedSeconds) {
                                    setAppSeconds(context, activePkg, 0)
                                    val appName = _activeAppName.value ?: getAppLabel(context, activePkg)

                                    _activeMonitoredPackage.value = null
                                    _activeAppSeconds.value = 0
                                    _isActivelyTracking.value = false
                                    lastActiveMs = 0L

                                    Log.i(TAG, "Screen time expired for $activePkg. Kicking to home!")
                                    blockCallback?.invoke(activePkg, appName)

                                    MonitorForegroundService.updateNotification(
                                        context = context,
                                        title = "$appName: 0m remaining",
                                        text = "Locked • Out of screen time"
                                    )
                                } else {
                                    val newSec = currentSec - elapsedSeconds
                                    setAppSeconds(context, activePkg, newSec)
                                    _activeAppSeconds.value = newSec

                                    if (newSec % 30 == 0 || newSec % 60 == 59) {
                                        val newMin = (newSec + 59) / 60
                                        updateDbAppTime(context, activePkg, newMin, newSec)
                                    }

                                    val appName = _activeAppName.value ?: getAppLabel(context, activePkg)
                                    val min = newSec / 60
                                    val sec = newSec % 60
                                    MonitorForegroundService.updateNotification(
                                        context = context,
                                        title = "$appName: ${min}m ${sec}s",
                                        text = "Counting down • Active in foreground"
                                    )
                                }
                            }
                        }
                    } else {
                        lastActiveMs = 0L
                        MonitorForegroundService.updateNotification(
                            context = context,
                            title = "Screen Time Monitor",
                            text = "Idle (Paused) • Not counting in background"
                        )
                    }
                }
            }
        }
    }

    /**
     * Called by AccessibilityService on window events.
     */
    fun onPackageVisible(context: Context, packageName: String) {
        val appContext = context.applicationContext
        if (!isInitialized.get()) init(appContext)

        // 1. Ignore transient system overlays (keyboard, notifications, volume panel)
        if (isTransientPackage(packageName)) {
            Log.d(TAG, "Ignoring transient package $packageName (active remains ${_activeMonitoredPackage.value})")
            return
        }

        // 2. Ignore our own app package
        if (packageName == appContext.packageName) {
            _activeMonitoredPackage.value = null
            return
        }

        // 3. Ignore launcher / home screen
        if (isLauncherPackage(appContext, packageName)) {
            _activeMonitoredPackage.value = null
            return
        }

        // 4. Check if this is a monitored blocked app
        trackerScope.launch {
            val db = AppDatabase.getInstance(appContext)
            val app = db.blockedAppDao().getByPackage(packageName)

            if (app != null && app.isBlocked) {
                val sec = getAppSeconds(appContext, packageName)

                if (sec <= 0) {
                    // Out of time for this specific app!
                    // KICK TO HOME SCREEN IMMEDIATELY. DO NOT TAKE THEM TO BLACKJACK.
                    Log.i(TAG, "Access blocked for $packageName (0s remaining). Sending to home.")
                    _activeMonitoredPackage.value = null
                    _isActivelyTracking.value = false
                    blockCallback?.invoke(packageName, app.appName)
                } else {
                    // Time available: start counting down for this app
                    Log.i(TAG, "Actively using monitored app $packageName. Remaining: ${sec}s")
                    _activeMonitoredPackage.value = packageName
                    _activeAppName.value = app.appName
                    _activeAppSeconds.value = sec
                }
            } else {
                // Non-monitored app: pause tracking
                _activeMonitoredPackage.value = null
            }
        }
    }

    fun getAppSeconds(context: Context, packageName: String): Int {
        val currentMap = _appSecondsMap.value
        if (currentMap.containsKey(packageName)) {
            return currentMap[packageName] ?: 0
        }
        val saved = getPrefs(context).getInt("sec_$packageName", -1)
        if (saved >= 0) return saved
        return 15 * 60 // Default 15m
    }

    fun setAppSeconds(context: Context, packageName: String, seconds: Int) {
        val clamped = seconds.coerceAtLeast(0)
        getPrefs(context).edit().putInt("sec_$packageName", clamped).apply()
        val updated = _appSecondsMap.value.toMutableMap()
        updated[packageName] = clamped
        _appSecondsMap.value = updated
        updateDbAppTime(context, packageName, (clamped + 59) / 60, clamped)
    }

    fun addAppMinutes(context: Context, packageName: String, minutesToAdd: Int) {
        val current = getAppSeconds(context, packageName)
        val newSec = (current + minutesToAdd * 60).coerceAtLeast(0)
        setAppSeconds(context, packageName, newSec)
        Log.d(TAG, "Added $minutesToAdd min to $packageName (new total: ${newSec / 60}m)")
    }

    private fun updateDbAppTime(context: Context, packageName: String, minutes: Int, seconds: Int) {
        trackerScope.launch {
            try {
                AppDatabase.getInstance(context).blockedAppDao().updateAppTime(packageName, minutes, seconds)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update app time in DB", e)
            }
        }
    }

    fun setScreenInteractive(interactive: Boolean) {
        isScreenInteractive = interactive
        Log.d(TAG, "Screen interactive status updated: $interactive")
    }

    private fun getAppLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }
    }

    fun isTransientPackage(packageName: String): Boolean {
        if (packageName == "com.android.systemui") return true
        if (packageName == "android") return true
        if (packageName.contains("permissioncontroller")) return true
        val lower = packageName.lowercase()
        if (lower.contains("inputmethod") || lower.contains("keyboard") || lower.contains("swiftkey") || lower.contains("honeyboard")) {
            return true
        }
        return false
    }

    fun isLauncherPackage(context: Context, packageName: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo?.activityInfo?.packageName == packageName) {
                return true
            }

            val homeApps = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            if (homeApps.any { it.activityInfo?.packageName == packageName }) {
                return true
            }

            val lower = packageName.lowercase()
            if (lower.contains("launcher") || lower.contains(".home") || lower.contains("quickstep") || lower.contains("trebuchet") || lower.contains("lawnchair")) {
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking launcher package", e)
        }
        return false
    }
}
