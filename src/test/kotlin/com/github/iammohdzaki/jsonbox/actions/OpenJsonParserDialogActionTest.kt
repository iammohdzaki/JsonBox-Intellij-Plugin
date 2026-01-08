package com.github.iammohdzaki.jsonbox.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenJsonParserDialogActionTest : BasePlatformTestCase() {

    fun testActionUpdate() {
        val action = OpenJsonParserDialogAction()
        val event = AnActionEvent.createFromAnAction(action, null, "test") { dataId ->
            if (com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT.`is`(dataId)) project else null
        }
        
        action.update(event)
        
        assertEquals("JSON Box", event.presentation.text)
        assertTrue(event.presentation.isEnabled)
    }

    fun testActionUpdateNoProject() {
        val action = OpenJsonParserDialogAction()
        val event = AnActionEvent.createFromAnAction(action, null, "test") { dataId ->
            if (com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT.`is`(dataId)) null else null
        }
        
        action.update(event)
        
        assertFalse(event.presentation.isEnabled)
    }
}
