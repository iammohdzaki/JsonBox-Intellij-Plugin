package com.github.iammohdzaki.jsonbox.actions

import com.github.iammohdzaki.jsonbox.dialog.JsonBoxQuickDialog
import com.github.iammohdzaki.jsonbox.utils.JsonBoxBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action to open the JSON Quick List dialog.
 * This dialog allows users to manage and quickly access saved JSON snippets.
 */
class OpenJsonQuickListAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        JsonBoxQuickDialog(project).apply { isVisible = true }
    }

    override fun update(e: AnActionEvent) {
        // Set text, description, and icon dynamically
        e.presentation.text = JsonBoxBundle.message("jsonbox.quick.title")
        e.presentation.icon = AllIcons.FileTypes.Json

        // Optionally disable action if no project is open
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}