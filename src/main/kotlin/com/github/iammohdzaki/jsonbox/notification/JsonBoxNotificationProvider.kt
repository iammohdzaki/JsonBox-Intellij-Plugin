package com.github.iammohdzaki.jsonbox.notification

import com.github.iammohdzaki.jsonbox.dialog.JsonBoxDialog
import com.github.iammohdzaki.jsonbox.persistance.JsonQuickListState
import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.github.iammohdzaki.jsonbox.utils.JsonBoxBundle
import com.intellij.json.JsonFileType
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import java.util.function.Function
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * Provides editor notifications for JSON files, allowing users to quickly open or add them to JsonBox.
 */
class JsonBoxNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<in FileEditor, out JComponent?>? {
        // Only show for JSON files
        if (file.fileType !is JsonFileType) return null
        val state = project.service<JsonQuickListState>()
        return Function { editor ->
            if (editor !is TextEditor) return@Function null

            val panel = EditorNotificationPanel()

            val isAdded = state.contains(file.presentableName)
            panel.text = JsonBoxBundle.message("jsonbox.notification.description")

            // "Open in JsonBox" action
            panel.createActionLabel(JsonBoxBundle.message("jsonbox.notification.title")) {
                val document = FileDocumentManager.getInstance().getDocument(file)
                val content = document?.text ?: file.inputStream.bufferedReader().use { it.readText() }
                
                // Show the JsonBoxDialog on the EDT since it creates UI components
                SwingUtilities.invokeLater {
                    JsonBoxDialog(project, file, JsonItem(title = file.presentableName, json = content)).apply { isVisible = true }
                }
            }

            // "Add to Quick List" action
            panel.createActionLabel(JsonBoxBundle.message(if (isAdded) "jsonbox.notification.added" else "jsonbox.notification.add")) {
                if (!isAdded) {
                    val document = FileDocumentManager.getInstance().getDocument(file)
                    val content = document?.text ?: file.inputStream.bufferedReader().use { it.readText() }
                    state.add(JsonItem(title = file.presentableName, json = content))

                    JsonBoxNotifications.notify(
                        project,
                        JsonBoxBundle.message("jsonbox.title"),
                        JsonBoxBundle.message("jsonbox.notification.added")
                    )
                    EditorNotifications.getInstance(project)
                        .updateNotifications(file)
                }
            }
            panel as JComponent
        }
    }
}