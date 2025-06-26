package com.github.iammohdzaki.jsonbox

import com.github.iammohdzaki.jsonbox.dialogs.JsonParserDialog
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

class JsonBoxNotificationProvider : EditorNotifications.Provider<EditorNotificationPanel>() {

    override fun getKey(): Key<EditorNotificationPanel> = KEY

    override fun createNotificationPanel(
        file: VirtualFile,
        fileEditor: FileEditor,
        project: Project
    ): EditorNotificationPanel? {
        if (file.fileType !is JsonFileType || fileEditor !is TextEditor) return null

        val panel = EditorNotificationPanel()
        panel.text = "You can open this JSON file in JsonBox for advanced formatting and validation."
        panel.createActionLabel("Open in JsonBox") {
            val document = FileDocumentManager.getInstance().getDocument(file)
            val content = document?.text ?: file.inputStream.bufferedReader().use { it.readText() }
            JsonParserDialog(content).show()
        }
        return panel
    }

    companion object {
        private val KEY = Key.create<EditorNotificationPanel>("JsonBox.Notification")
    }
}