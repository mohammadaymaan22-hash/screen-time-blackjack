package com.placeholder.screentimeblackjack.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.placeholder.screentimeblackjack.MainActivity
import com.placeholder.screentimeblackjack.data.AppDatabase
import com.placeholder.screentimeblackjack.service.AppBlockerAccessibilityService
import com.placeholder.screentimeblackjack.service.MonitorForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton managing second-by-second screen time tracking and enforcement.
 *
 * Rules:
 * 1. Decrements time ONLY when a monitored blocked app is actively in the foreground on screen.
 * 2. Pauses immediately if the app is in the background / memory, on Home screen, or if screen is off.
 * 3. Filters out transient system overlays (keyboards, notifications, volume bar) so they don't break tracking.
 * 4. When balance reaches 0s, immediately intercepts and returns the user to Screen Time Blackjack.
 */
object UsageTimeTracker {

    private const val TAG = "UsageTimeTracker"
    private const val PREFS_NAME = "screen_time_tracker_prefs"
    private const val KEY_BALANCE_SECONDS = "balance_seconds"

    private val trackerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null

    private val _secondsRemaining = MutableStateFlow(3600) // Default 60 min (3600s)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _activeMonitoredPackage = MutableStateFlow<String?>(null)
    val activeMonitoredPackage: StateFlow<String?> = _activeMonitoredPackage.asStateFlow()

    private val _isActivelyTracking = MutableStateFlow(false)
    val isActivelyTracking: StateFlow<Boolean> = _isActivelyTracking.asStateFlow()

    @Volatile
    private var isScreenInteractive: Boolean = true

    @Volatile
    private var isInitialized: Boolean = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext

        trackerScope.launch {
            val db = AppDatabase.getInstance(appContext)
            val playerState = db.playerStateDao().get()
            val dbMinutes = playerState?.timeBalance ?: 60

            val prefs = getPrefs(appContext)
            val savedSeconds = prefs.getInt(KEY_BALANCE_SECONDS, -1)

            val initialSeconds = if (savedSeconds >= 0) {
                // If saved seconds diverge significantly from db minutes, align them
                val savedMin = (savedSeconds + 59) / 60
                if (savedMin == dbMinutes) savedSeconds else dbMinutes * 60
            } else {
                dbMinutes * 60
            }

            _secondsRemaining.value = initialSeconds
            prefs.edit().putInt(KEY_BALANCE_SECONDS, initialSeconds).apply()
            isInitialized = true
            Log.d(TAG, "UsageTimeTracker initialized with $initialSeconds seconds (${initialSeconds / 60}m)")

            startTickerLoop(appContext)
        }
    }

    private fun startTickerLoop(context: Context) {
        tickerJob?.cancel()
        tickerJob = trackerScope.launch {
            var lastDbSyncSeconds = _secondsRemaining.value

            while (isActive) {
                delay(1000L)

                val activePkg = _activeMonitoredPackage.value
                val isTrackingNow = activePkg != null && isScreenInteractive

                _isActivelyTracking.value = isTrackingNow

                if (isTrackingNow) {
                    val currentSec = _secondsRemaining.value
                    if (currentSec <= 1) {
                        // Expired!
                        _secondsRemaining.value = 0
                        getPrefs(context).edit().putInt(KEY_BALANCE_SECONDS, 0).apply()
                        updateDbMinutes(context, 0)

                        Log.i(TAG, "Screen time balance reached 0 while using $activePkg. Gating access!")
                        _activeMonitoredPackage.value = null
                        _isActivelyTracking.value = false

                        activePkg?.let { launchGatingScreen(context, it) }
                        MonitorForegroundService.updateNotification(
                            context = context,
                            title = "Screen Time Depleted",
                            text = "0m remaining • Device access locked"
                        )
                    } else {
                        val newSec = currentSec - 1
                        _secondsRemaining.value = newSec
                        getPrefs(context).edit().putInt(KEY_BALANCE_SECONDS, newSec).apply()

                        // Sync to Room on every 30s or minute boundary
                        if (newSec % 30 == 0 || (newSec / 60) != (lastDbSyncSeconds / 60)) {
                            val newMin = (newSec + 59) / 60
                            updateDbMinutes(context, newMin)
                            lastDbSyncSeconds = newSec
                        }

                        val appName = getAppLabel(context, activePkg!!)
                        val min = newSec / 60
                        val sec = newSec % 60
                        MonitorForegroundService.updateNotification(
                            context = context,
                            title = "Screen Time Active: ${min}m ${sec}s",
                            text = "$appName is in use"
                        )
                    }
                } else {
                    // Idle / paused
                    val curSec = _secondsRemaining.value
                    val min = curSec / 60
                    val sec = curSec % 60
                    MonitorForegroundService.updateNotification(
                        context = context,
                        title = "Screen Time: ${min}m ${sec}s remaining",
                        text = "Idle (Paused) • Not counting in background"
                    )
                }
            }
        }
    }

    /**
     * Called by AccessibilityService on window state/content change events.
     */
    fun onPackageVisible(context: Context, packageName: String) {
        val appContext = context.applicationContext
        if (!isInitialized) init(appContext)

        // 1. Ignore transient system overlays (keyboard, notifications, volume panel)
        if (isTransientPackage(packageName)) {
            Log.d(TAG, "Ignoring transient system package: $packageName (active remains ${_activeMonitoredPackage.value})")
            return
        }

        // 2. Ignore our own app package
        if (packageName == appContext.packageName) {
            Log.d(TAG, "Screen Time Blackjack in foreground -> Pausing timer")
            _activeMonitoredPackage.value = null
            return
        }

        // 3. Ignore launcher / home screen
        if (isLauncherPackage(appContext, packageName)) {
            Log.d(TAG, "Home screen / launcher in foreground -> Pausing timer (app in background/memory)")
            _activeMonitoredPackage.value = null
            return
        }

        // 4. Check if the app is on the blocked list
        trackerScope.launch {
            val db = AppDatabase.getInstance(appContext)
            val isBlocked = db.blockedAppDao().isAppBlocked(packageName)

            if (isBlocked) {
                val currentSec = _secondsRemaining.value
                if (currentSec <= 0) {
                    // Balance is already 0: intercept immediately!
                    Log.i(TAG, "Access intercepted for $packageName (balance is 0)")
                    _activeMonitoredPackage.value = null
                    launchGatingScreen(appContext, packageName)
                } else {
                    // Balance > 0: start active decrement
                    Log.i(TAG, "Monitored app in active use: $packageName. Countdown active.")
                    _activeMonitoredPackage.value = packageName
                }
            } else {
                // Non-monitored third-party app: pause tracking
                _activeMonitoredPackage.value = null
            }
        }
    }

    fun setScreenInteractive(interactive: Boolean) {
        isScreenInteractive = interactive
        Log.d(TAG, "Screen interactive status updated: $interactive")
    }

    /**
     * Synchronize balance from Blackjack game (when betting, winning, or adding emergency time).
     */
    fun setBalanceMinutes(context: Context, minutes: Int) {
        val seconds = (minutes * 60).coerceAtLeast(0)
        _secondsRemaining.value = seconds
        getPrefs(context).edit().putInt(KEY_BALANCE_SECONDS, seconds).apply()
        updateDbMinutes(context, minutes)
        Log.d(TAG, "Balance synchronized to $minutes min ($seconds seconds)")
    }

    private fun updateDbMinutes(context: Context, minutes: Int) {
        trackerScope.launch {
            try {
                AppDatabase.getInstance(context).playerStateDao().updateBalance(minutes)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update balance in DB", e)
            }
        }
    }

    private fun launchGatingScreen(context: Context, packageName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AppBlockerAccessibilityService.EXTRA_BLOCKED_APP_TRIGGERED, packageName)
        }
        context.startActivity(intent)
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
        // Keyboards / IME
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
