package com.placeholder.screentimeblackjack.util

import org.junit.Assert.*
import org.junit.Test

class UsageTimeTrackerTest {

    @Test
    fun testTransientPackagesAreIgnored() {
        // System UI
        assertTrue(UsageTimeTracker.isTransientPackage("com.android.systemui"))
        assertTrue(UsageTimeTracker.isTransientPackage("android"))
        assertTrue(UsageTimeTracker.isTransientPackage("com.google.android.permissioncontroller"))

        // Keyboards
        assertTrue(UsageTimeTracker.isTransientPackage("com.google.android.inputmethod.latin"))
        assertTrue(UsageTimeTracker.isTransientPackage("com.samsung.android.honeyboard"))
        assertTrue(UsageTimeTracker.isTransientPackage("com.touchtype.swiftkey"))

        // Monitored Apps must NOT be transient
        assertFalse(UsageTimeTracker.isTransientPackage("com.google.android.youtube"))
        assertFalse(UsageTimeTracker.isTransientPackage("com.instagram.android"))
        assertFalse(UsageTimeTracker.isTransientPackage("com.zhiliaoapp.musically"))
        assertFalse(UsageTimeTracker.isTransientPackage("com.twitter.android"))
    }
}
