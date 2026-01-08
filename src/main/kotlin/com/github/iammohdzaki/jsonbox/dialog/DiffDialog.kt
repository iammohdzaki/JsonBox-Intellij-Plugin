package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.utils.JsonBoxBundle
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project

/**
 * Displays a diff dialog to compare two JSON strings.
 *
 * @param project The current IntelliJ project.
 * @param leftText The JSON content to show on the left side of the comparison.
 * @param rightText The JSON content to show on the right side of the comparison.
 */
fun showEditableJsonCompareDialog(project: Project, leftText: String = "", rightText: String = "") {
    val contentFactory = DiffContentFactory.getInstance()
    val editorFactory = EditorFactory.getInstance()

    val leftDocument = editorFactory.createDocument(leftText)
    val leftContent: DocumentContent = contentFactory.create(project, leftDocument)

    // Right side: empty editable document or prefilled text
    val rightDocument = editorFactory.createDocument(rightText)
    val rightContent: DocumentContent = contentFactory.create(project, rightDocument)

    // Create diff request
    val diffRequest = SimpleDiffRequest(
        JsonBoxBundle.message("jsonbox.diff.title"),
        leftContent,
        rightContent,
        JsonBoxBundle.message("jsonbox.diff.left"),
        JsonBoxBundle.message("jsonbox.diff.right")
    )

    // Show the diff dialog
    DiffManager.getInstance().showDiff(project, diffRequest)
}
