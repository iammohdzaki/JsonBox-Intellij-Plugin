package com.github.iammohdzaki.jsonbox.dialog

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JsonBoxDialogTest : BasePlatformTestCase() {

    fun testDialogInitialization() {
        val dialog = JsonBoxDialog(project, null)
        try {
            // dialog.init() is called by the constructor, which creates the center panel
            assertNotNull(dialog.title)
        } finally {
            Disposer.dispose(dialog.disposable)
        }
    }

    fun testDialogWithInitialContent() {
        val initialJson = """{"test": "value"}"""
        val dialog = JsonBoxDialog(project, null, com.github.iammohdzaki.jsonbox.persistance.model.JsonItem("Test Title", initialJson))
        try {
            assertNotNull(dialog.title)
        } finally {
            Disposer.dispose(dialog.disposable)
        }
    }
}
