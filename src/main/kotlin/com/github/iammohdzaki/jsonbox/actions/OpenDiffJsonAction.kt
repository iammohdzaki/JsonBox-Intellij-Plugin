package com.github.iammohdzaki.jsonbox.actions

import com.github.iammohdzaki.jsonbox.dialog.showEditableJsonCompareDialog
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action to open the JSON Diff dialog.
 * This dialog allows users to compare two JSON contents and see the differences.
 */
class OpenDiffJsonAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        showEditableJsonCompareDialog(project)
    }

    override fun update(e: AnActionEvent) {
        // Set text, description, and icon dynamically
        e.presentation.icon = AllIcons.FileTypes.Json

        // Optionally disable action if no project is open
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return super.getActionUpdateThread()
    }
}