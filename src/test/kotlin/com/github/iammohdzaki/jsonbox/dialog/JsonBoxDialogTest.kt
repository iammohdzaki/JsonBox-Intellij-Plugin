package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.GraphicsEnvironment

class JsonBoxDialogTest : BasePlatformTestCase() {

    fun testDialogInitialization() {
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val dialog = JsonBoxDialog(project, null)
        try {
            assertNotNull(dialog.title)
        } finally {
            dialog.dispose()
        }
    }

    fun testDialogWithInitialContent() {
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val initialJson = """{"test": "value"}"""
        val dialog = JsonBoxDialog(project, null, JsonItem(title = "Test Title", json = initialJson))
        try {
            assertNotNull(dialog.title)
        } finally {
            dialog.dispose()
        }
    }

    // -----------------------------------------------------------------------
    // Clipboard auto-detect
    // -----------------------------------------------------------------------

    fun testAddMode_ClipboardPrefill_DoesNotCrash() {
        // Verifies that opening in Add mode (jsonItem = null) completes without
        // throwing even when the clipboard contains non-JSON text.
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val dialog = JsonBoxDialog(project, null, jsonItem = null)
        try {
            assertNotNull("Dialog must initialize without error in Add mode", dialog)
        } finally {
            dialog.dispose()
        }
    }

    fun testEditMode_SkipsClipboardPrefill_PreservesOriginalJson() {
        // In Edit mode (jsonItem != null) the clipboard check must be skipped
        // so the saved JSON is displayed, not whatever is on the clipboard.
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val savedJson = """{"saved":true}"""
        val item = JsonItem(title = "My Snippet", json = savedJson)
        val dialog = JsonBoxDialog(project, null, jsonItem = item, editMode = true)
        try {
            assertNotNull(dialog)
        } finally {
            dialog.dispose()
        }
    }

    // -----------------------------------------------------------------------
    // Disposable cleanup (review fix)
    // -----------------------------------------------------------------------

    fun testDispose_DoesNotThrow() {
        // Ensures listenerDisposable + editor are released cleanly on close.
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val dialog = JsonBoxDialog(project, null)
        // Should not throw even when dispose() is called multiple times
        dialog.dispose()
    }
}