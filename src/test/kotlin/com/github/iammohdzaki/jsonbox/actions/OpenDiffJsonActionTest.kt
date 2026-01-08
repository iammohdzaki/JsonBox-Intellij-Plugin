package com.github.iammohdzaki.jsonbox.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenDiffJsonActionTest : BasePlatformTestCase() {

    fun testActionUpdate() {
        val action = OpenDiffJsonAction()
        val event = AnActionEvent.createFromAnAction(action, null, "test") { dataId ->
            if (CommonDataKeys.PROJECT.`is`(dataId)) project else null
        }
        
        action.update(event)
        
        assertTrue(event.presentation.isEnabled)
    }

    fun testActionUpdateNoProject() {
        val action = OpenDiffJsonAction()
        val event = AnActionEvent.createFromAnAction(action, null, "test") { dataId ->
            if (CommonDataKeys.PROJECT.`is`(dataId)) null else null
        }
        
        action.update(event)
        
        assertFalse(event.presentation.isEnabled)
    }
}
