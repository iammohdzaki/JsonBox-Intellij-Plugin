package com.github.iammohdzaki.jsonbox.actions

import com.github.iammohdzaki.jsonbox.dialog.JsonBoxDialog
import com.github.iammohdzaki.jsonbox.utils.StringUtils
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenJsonParserDialogAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.let {
            JsonBoxDialog(it, virtualFile = null).show()
        }
    }

    override fun update(e: AnActionEvent) {
        // Set text, description, and icon dynamically
        e.presentation.text = StringUtils.PLUGIN_NAME
        e.presentation.description = StringUtils.PLUGIN_DESCRIPTION
        e.presentation.icon = AllIcons.FileTypes.Json

        // Optionally disable action if no project is open
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}