package com.github.iammohdzaki.jsonbox.actions

import com.github.iammohdzaki.jsonbox.dialog.JsonBoxDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenJsonBoxWindowAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        JsonBoxDialog(project, null).apply { isVisible = true }
    }
}