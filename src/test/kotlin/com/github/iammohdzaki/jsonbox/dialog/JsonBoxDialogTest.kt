package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.GraphicsEnvironment
import java.awt.datatransfer.StringSelection

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

    fun testAddMode_ValidJsonOnClipboard_PrefillsEditor() {
        // Place valid JSON on the clipboard, open in Add mode, and assert the
        // editor text was populated from it (proving prefill actually fired).
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val clipJson = """{"source":"clipboard","value":42}"""
        CopyPasteManager.getInstance().setContents(StringSelection(clipJson))

        val dialog = JsonBoxDialog(project, null, jsonItem = null)
        try {
            val editorText = dialog.editorText.trim()
            assertFalse(
                "Editor must not be empty when clipboard holds valid JSON",
                editorText.isEmpty()
            )
            // The prefilled text is pretty-printed, so key tokens must be present
            assertTrue(
                "Editor must contain clipboard JSON key 'source'",
                editorText.contains("\"source\"")
            )
            assertTrue(
                "Editor must contain clipboard JSON value 'clipboard'",
                editorText.contains("\"clipboard\"")
            )
            assertTrue(
                "Editor must contain clipboard JSON key 'value'",
                editorText.contains("42")
            )
        } finally {
            dialog.dispose()
        }
    }

    fun testAddMode_NonJsonOnClipboard_EditorRemainsEmpty() {
        // Place plain text on the clipboard; the editor must stay empty because
        // non-JSON clipboard content must never trigger prefill.
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        CopyPasteManager.getInstance().setContents(StringSelection("Hello, this is plain text!"))

        val dialog = JsonBoxDialog(project, null, jsonItem = null)
        try {
            assertTrue(
                "Editor must be empty when clipboard does not contain valid JSON",
                dialog.editorText.isBlank()
            )
        } finally {
            dialog.dispose()
        }
    }

    fun testEditMode_ValidJsonOnClipboard_EditorShowsSavedJson() {
        // In Edit mode the clipboard must be ignored entirely. Even if the clipboard
        // holds valid JSON, the editor must show the saved JsonItem's content.
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val savedJson = """{"saved":true}"""
        val clipJson  = """{"fromClipboard":true}"""
        CopyPasteManager.getInstance().setContents(StringSelection(clipJson))

        val item = JsonItem(title = "My Snippet", json = savedJson)
        val dialog = JsonBoxDialog(project, null, jsonItem = item, editMode = true)
        try {
            val editorText = dialog.editorText
            assertTrue(
                "Editor must show the saved JSON, not clipboard content",
                editorText.contains("\"saved\"")
            )
            assertFalse(
                "Clipboard content must not appear in edit-mode editor",
                editorText.contains("\"fromClipboard\"")
            )
        } finally {
            dialog.dispose()
        }
    }

    // -----------------------------------------------------------------------
    // Disposable cleanup (review fix)
    // -----------------------------------------------------------------------

    fun testDispose_IsIdempotent_DoesNotThrow() {
        // Ensures listenerDisposable + editor are released cleanly, and that
        // calling dispose() a second time does not throw.
        if (GraphicsEnvironment.isHeadless()) {
            println("Skipping test in headless environment.")
            return
        }
        val dialog = JsonBoxDialog(project, null)
        dialog.dispose()
        dialog.dispose() // second call must also be safe
    }
}