package com.github.iammohdzaki.jsonbox.dialog

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JsonBoxQuickDialogTest : BasePlatformTestCase() {

    fun testQuickDialogInitialization() {
        val dialog = JsonBoxQuickDialog(project)
        try {
            // Check if the dialog initializes without errors
            assertNotNull(dialog)
        } finally {
            Disposer.dispose(dialog.disposable)
        }
    }
}
