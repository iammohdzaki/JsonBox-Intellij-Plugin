package com.github.iammohdzaki.jsonbox.dialog

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
        val dialog = JsonBoxDialog(project, null, com.github.iammohdzaki.jsonbox.persistance.model.JsonItem("Test Title", initialJson))
        try {
            assertNotNull(dialog.title)
        } finally {
            dialog.dispose()
        }
    }
}