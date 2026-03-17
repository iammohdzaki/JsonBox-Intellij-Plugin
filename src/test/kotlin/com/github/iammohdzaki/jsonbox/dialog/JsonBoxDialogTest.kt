package com.github.iammohdzaki.jsonbox.dialog

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JsonBoxDialogTest : BasePlatformTestCase() {

    fun testDialogInitialization() {
        val dialog = JsonBoxDialog(project, null)
        try {
            assertNotNull(dialog.title)
        } finally {
            dialog.dispose()
        }
    }

    fun testDialogWithInitialContent() {
        val initialJson = """{"test": "value"}"""
        val dialog = JsonBoxDialog(project, null, com.github.iammohdzaki.jsonbox.persistance.model.JsonItem("Test Title", initialJson))
        try {
            assertNotNull(dialog.title)
        } finally {
            dialog.dispose()
        }
    }
}