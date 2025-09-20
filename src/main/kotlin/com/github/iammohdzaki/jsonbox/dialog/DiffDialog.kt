package com.github.iammohdzaki.jsonbox.dialog

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun showEditableJsonCompareDialog(project: Project, leftFile: VirtualFile?, rightText: String = "") {
    val contentFactory = DiffContentFactory.getInstance()
    val editorFactory = EditorFactory.getInstance()

    // Left side: file content or empty document
    val leftDocument = if (leftFile != null) {
        val text = leftFile.inputStream.bufferedReader().use { it.readText() }
        editorFactory.createDocument(text)
    } else {
        editorFactory.createDocument("")
    }
    val leftContent: DocumentContent = contentFactory.create(project, leftDocument)

    // Right side: empty editable document or prefilled text
    val rightDocument = editorFactory.createDocument(rightText)
    val rightContent: DocumentContent = contentFactory.create(project, rightDocument)

    // Create diff request
    val diffRequest = SimpleDiffRequest(
        "Editable JSON Compare",
        leftContent,
        rightContent,
        leftFile?.name ?: "Left JSON",
        "Right JSON"
    )

    // Show the diff dialog
    DiffManager.getInstance().showDiff(project, diffRequest)
}
