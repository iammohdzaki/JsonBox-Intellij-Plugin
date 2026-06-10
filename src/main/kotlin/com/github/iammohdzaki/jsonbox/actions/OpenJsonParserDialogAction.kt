package com.github.iammohdzaki.jsonbox.actions

import com.github.iammohdzaki.jsonbox.dialog.JsonBoxDialog
import com.github.iammohdzaki.jsonbox.utils.JsonBoxBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action to open the main JSON Parser dialog.
 * This dialog provides tools for parsing, formatting, and validating JSON content.
 */
class OpenJsonParserDialogAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.let {
            JsonBoxDialog(it, virtualFile = null).apply { isVisible = true }
        }
    }

    override fun update(e: AnActionEvent) {
        // Set text, description, and icon dynamically
        e.presentation.text = JsonBoxBundle.message("jsonbox.title")
        e.presentation.description = JsonBoxBundle.message("jsonbox.description")
        e.presentation.icon = AllIcons.FileTypes.Json

        // Optionally disable action if no project is open
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}