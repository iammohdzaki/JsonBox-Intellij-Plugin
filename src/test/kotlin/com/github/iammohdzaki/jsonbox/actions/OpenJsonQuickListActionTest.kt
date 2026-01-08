package com.github.iammohdzaki.jsonbox.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenJsonQuickListActionTest : BasePlatformTestCase() {

    fun testActionUpdate() {
        val action = OpenJsonQuickListAction()
        val event = AnActionEvent.createFromAnAction(action, null, "test") { dataId ->
            if (CommonDataKeys.PROJECT.`is`(dataId)) project else null
        }
        
        action.update(event)
        
        assertEquals("JsonBox Quick List", event.presentation.text)
        assertTrue(event.presentation.isEnabled)
    }

    fun testActionUpdateNoProject() {
        val action = OpenJsonQuickListAction()
        val event = AnActionEvent.createFromAnAction(action, null, "test") { dataId ->
            if (CommonDataKeys.PROJECT.`is`(dataId)) null else null
        }
        
        action.update(event)
        
        assertFalse(event.presentation.isEnabled)
    }
}
