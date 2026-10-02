package com.placeholder.screentimeblackjack.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.placeholder.screentimeblackjack.util.UsageTimeTracker

/**
 * Real-time foreground app monitoring via AccessibilityService.
 *
 * When an app is out of time (0 min), intercepts access by immediately triggering
 * [GLOBAL_ACTION_HOME] and displaying a brief toast notice. Does not hijack the screen
 * with Screen Time Casino.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private var screenReceiver: BroadcastReceiver? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AppBlockerAccessibilityService connected")
        Companion.isServiceRunning = true

        // Wire kick-to-home callback
        UsageTimeTracker.blockCallback = { packageName, appName ->
            blockAndSendHome(packageName, appName)
        }

        // Initialize per-app time tracker
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

        // 1. Process package event
        UsageTimeTracker.onPackageVisible(this, pkgName)

        // 2. Also verify against the active root window (ground truth)
        try {
            val rootPkg = rootInActiveWindow?.packageName?.toString()
            if (rootPkg != null && rootPkg != pkgName) {
                UsageTimeTracker.onPackageVisible(this, rootPkg)
            }
        } catch (e: Exception) {
            // Ignore accessibility hierarchy query exceptions
        }
    }

    private fun blockAndSendHome(packageName: String, appName: String) {
        Log.i(TAG, "Gating access to $packageName ($appName): returning to HOME screen")

        // 1. Return to Home screen immediately
        performGlobalAction(GLOBAL_ACTION_HOME)

        // 2. Show brief non-intrusive Toast notice on main thread
        Handler(Looper.getMainLooper()).post {
            try {
                Toast.makeText(
                    applicationContext,
                    "$appName is locked (0 min remaining)",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.w(TAG, "Could not show block toast", e)
            }
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
        UsageTimeTracker.blockCallback = null
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
