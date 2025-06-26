package com.github.iammohdzaki.jsonbox.actions

import com.github.iammohdzaki.jsonbox.dialogs.JsonParserDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenJsonParserDialogAction : AnAction("Open JSON Parser") {

    override fun actionPerformed(event: AnActionEvent) {
        JsonParserDialog().show()
    }
}