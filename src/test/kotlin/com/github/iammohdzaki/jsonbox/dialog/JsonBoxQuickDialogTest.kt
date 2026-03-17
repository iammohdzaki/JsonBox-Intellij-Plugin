package com.github.iammohdzaki.jsonbox.dialog

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.GraphicsEnvironment

class JsonBoxQuickDialogTest : BasePlatformTestCase() {

    fun testQuickDialogInitialization() {
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val dialog = JsonBoxQuickDialog(project)
        try {
            // Check if the dialog initializes without errors
            assertNotNull(dialog)
        } finally {
            dialog.dispose()
        }
    }
}