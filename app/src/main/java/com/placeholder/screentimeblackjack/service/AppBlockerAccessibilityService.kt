package com.placeholder.screentimeblackjack.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.placeholder.screentimeblackjack.util.UsageTimeTracker

/**
 * Real-time foreground app monitoring via AccessibilityService.
 *
 * Intercepts window state and window hierarchy changes to determine which app is actively
 * on screen and being used by the user. Delegates countdown, pause-on-background,
 * and access gating to [UsageTimeTracker].
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private var screenReceiver: BroadcastReceiver? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AppBlockerAccessibilityService connected")
        Companion.isServiceRunning = true

        // Initialize second-by-second usage tracker
        UsageTimeTracker.init(this)

        // Automatically start the foreground keepalive service with status notification
        try {
            MonitorForegroundService.start(this)
        } catch (e: Exception) {
            Log.w(TAG, "Could not start MonitorForegroundService", e)
        }

        // Register screen on/off receiver to pause timer when phone screen turns off
        registerScreenStateReceiver()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return

        // 1. Process package visible event via UsageTimeTracker
        UsageTimeTracker.onPackageVisible(this, pkgName)

        // 2. Also verify against the active root window (ground truth)
        try {
            val rootPkg = rootInActiveWindow?.packageName?.toString()
            if (rootPkg != null && rootPkg != pkgName) {
                UsageTimeTracker.onPackageVisible(this, rootPkg)
            }
        } catch (e: Exception) {
            // Ignore security or accessibility hierarchy query exceptions
        }
    }

    private fun registerScreenStateReceiver() {
        if (screenReceiver != null) return

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        Log.d(TAG, "Broadcast received: SCREEN OFF -> Pausing active timer")
                        UsageTimeTracker.setScreenInteractive(false)
                    }
                    Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                        Log.d(TAG, "Broadcast received: SCREEN ON -> Resuming timer check")
                        UsageTimeTracker.setScreenInteractive(true)
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        registerReceiver(receiver, filter)
        screenReceiver = receiver
        Log.d(TAG, "Screen state broadcast receiver registered successfully")
    }

    override fun onInterrupt() {
        Log.w(TAG, "AppBlockerAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering screen receiver", e)
            }
            screenReceiver = null
        }
        Companion.isServiceRunning = false
        Log.i(TAG, "AppBlockerAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "AppBlockerAccessibility"
        const val EXTRA_BLOCKED_APP_TRIGGERED = "com.placeholder.screentimeblackjack.BLOCKED_APP"

        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }
}
