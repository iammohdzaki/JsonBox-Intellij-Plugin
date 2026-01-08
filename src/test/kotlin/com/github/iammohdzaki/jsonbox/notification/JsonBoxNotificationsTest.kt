package com.github.iammohdzaki.jsonbox.notification

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JsonBoxNotificationsTest : BasePlatformTestCase() {

    fun testNotify() {
        // We can't easily verify that a notification was actually shown in a platform test
        // without more complex mocking, but we can verify that the call doesn't throw exceptions.
        try {
            JsonBoxNotifications.notify(project, "Test Title", "Test Content")
        } catch (e: Exception) {
            fail("Notification should not throw exception: ${e.message}")
        }
    }
}
