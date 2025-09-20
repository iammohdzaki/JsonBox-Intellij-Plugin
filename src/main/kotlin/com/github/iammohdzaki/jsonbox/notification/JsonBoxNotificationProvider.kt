package com.github.iammohdzaki.jsonbox.notification

import com.github.iammohdzaki.jsonbox.dialog.JsonBoxDialog
import com.github.iammohdzaki.jsonbox.utils.StringUtils
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

class JsonBoxNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<in FileEditor, out JComponent?>? {
        // Only show for JSON files
        if (file.fileType !is JsonFileType) return null

        return Function { editor ->
            if (editor !is TextEditor) return@Function null

            val panel = EditorNotificationPanel()
            panel.text = StringUtils.NOTIFICATION_DESCRIPTION
            panel.createActionLabel(StringUtils.OPEN_IN_JSON_BOX) {
                val document = FileDocumentManager.getInstance().getDocument(file)
                val content = document?.text ?: file.inputStream.bufferedReader().use { it.readText() }
                JsonBoxDialog(project, file, content).show()
            }
            panel as JComponent
        }
    }
}