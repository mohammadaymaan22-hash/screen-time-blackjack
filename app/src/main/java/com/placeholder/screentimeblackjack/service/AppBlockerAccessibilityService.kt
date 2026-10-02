package com.placeholder.screentimeblackjack.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.placeholder.screentimeblackjack.MainActivity
import com.placeholder.screentimeblackjack.data.AppDatabase
import kotlinx.coroutines.*

/**
 * Real-time foreground app monitoring via AccessibilityService.
 *
 * Intercepts [AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED] to detect when user launches
 * or switches to a monitored app.
 *
 * If balance > 0, tracks foreground time in 60-second increments, decrementing the time balance.
 * If balance <= 0, immediately launches [MainActivity] over the blocked application to gate access.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var trackingJob: Job? = null

    @Volatile
    private var currentForegroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AppBlockerAccessibilityService connected")
        Companion.isServiceRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val pkgName = event.packageName?.toString() ?: return

        // Ignore our own app package and system UI/launcher transitions
        if (pkgName == applicationContext.packageName) {
            stopTimeTracking()
            currentForegroundPackage = null
            return
        }

        // Avoid re-processing the same foreground app continually
        if (pkgName == currentForegroundPackage) {
            return
        }

        currentForegroundPackage = pkgName
        handlePackageChange(pkgName)
    }

    private fun handlePackageChange(packageName: String) {
        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val isBlockedApp = db.blockedAppDao().isAppBlocked(packageName)

            if (!isBlockedApp) {
                stopTimeTracking()
                return@launch
            }

            val playerState = db.playerStateDao().get()
            val currentBalance = playerState?.timeBalance ?: 0

            if (currentBalance <= 0) {
                // Balance depleted: gate access immediately
                stopTimeTracking()
                launchGatingScreen(packageName)
            } else {
                // Balance available: initiate active time decrementing
                startTimeTracking(packageName)
            }
        }
    }

    private fun startTimeTracking(monitoredPackage: String) {
        stopTimeTracking()
        trackingJob = serviceScope.launch {
            Log.d(TAG, "Starting screen-time decrement timer for $monitoredPackage")
            while (isActive) {
                delay(TICK_INTERVAL_MS)

                val db = AppDatabase.getInstance(applicationContext)
                val playerState = db.playerStateDao().get()
                val currentBalance = playerState?.timeBalance ?: 0

                if (currentBalance <= 1) {
                    // Decrement final minute and trigger block immediately
                    val newBalance = (currentBalance - 1).coerceAtLeast(0)
                    db.playerStateDao().updateBalance(newBalance)
                    Log.i(TAG, "Time balance expired while using $monitoredPackage. Triggering block!")
                    launchGatingScreen(monitoredPackage)
                    break
                } else {
                    // Decrement 1 minute
                    val newBalance = currentBalance - 1
                    db.playerStateDao().updateBalance(newBalance)
                    Log.d(TAG, "Deducted 1 minute for $monitoredPackage. Remaining: $newBalance min")
                }
            }
        }
    }

    private fun stopTimeTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private fun launchGatingScreen(packageName: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_BLOCKED_APP_TRIGGERED, packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "AppBlockerAccessibilityService interrupted")
        stopTimeTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimeTracking()
        serviceScope.cancel()
        Companion.isServiceRunning = false
        Log.i(TAG, "AppBlockerAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "AppBlockerAccessibility"
        const val EXTRA_BLOCKED_APP_TRIGGERED = "com.placeholder.screentimeblackjack.BLOCKED_APP"
        private const val TICK_INTERVAL_MS = 60_000L // 1 minute interval

        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }
}
