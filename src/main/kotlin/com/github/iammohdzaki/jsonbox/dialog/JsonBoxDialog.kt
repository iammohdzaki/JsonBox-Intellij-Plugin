package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.components.ButtonFactory
import com.github.iammohdzaki.jsonbox.editor.JsonEditorFactory
import com.github.iammohdzaki.jsonbox.utils.JsonUtils
import com.intellij.find.EditorSearchSession
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.Action
import javax.swing.JComponent

/**
 * JsonBoxDialog
 *
 * A custom dialog for editing JSON files or arbitrary JSON text in IntelliJ.
 * Features:
 * - PSI-backed JSON editor with syntax highlighting, code folding, line numbers
 * - Buttons for Validate, Format, Save, Stringify, DeStringify, Copy, Search
 * - Save occurs only when the user clicks the Save button
 *
 * @param project The current IntelliJ project
 * @param virtualFile Optional VirtualFile; null if opened without a file
 * @param initialText Initial JSON text to populate the editor
 */
class JsonBoxDialog(
    private val project: Project,
    private val virtualFile: VirtualFile?,          // Null if opened without a file
    private val initialText: String = ""
) : DialogWrapper(true) {

    // Create the editor instance (syntax-highlighted, folding enabled)
    private val editor: EditorEx = JsonEditorFactory.createEditor(project, virtualFile, initialText)


    // -------------------
    // Button Definitions
    // -------------------

    // Search button: opens the IntelliJ search overlay in the editor
    private val searchButton =
        ButtonFactory.createNormalButton("Search", AllIcons.Actions.Search, "Search JSON (Ctrl+F)") {
            EditorSearchSession.start(editor, project)
        }

    // Validate button: checks if current JSON is valid and shows a message
    private val validateButton = ButtonFactory.createNormalButton("Validate", AllIcons.Actions.Checked) {
        val error = JsonUtils.validateJson(editor.document.text)
        if (error == null) {
            Messages.showInfoMessage("Valid JSON", "Validation")
        } else {
            Messages.showInfoMessage("Invalid JSON $error", "Validation")
        }
    }

    // Format button: pretty-prints JSON, replaces text in editor if valid
    private val formatButton = ButtonFactory.createNormalButton("Format", AllIcons.Actions.ChangeView) {
        val text = editor.document.text
        if (text.isBlank()) {
            Messages.showErrorDialog("Nothing to format: JSON is empty", "Formatting Error")
            return@createNormalButton
        }

        WriteCommandAction.runWriteCommandAction(project) {
            try {
                // Create a temporary PSI file if editor is not backed by a real file
                val psiFile = if (editor.virtualFile != null) {
                    PsiManager.getInstance(project).findFile(editor.virtualFile!!)!!
                } else {
                    PsiFileFactory.getInstance(project)
                        .createFileFromText("temp.json", JsonLanguage.INSTANCE, text)
                }

                // Format using CodeStyleManager
                CodeStyleManager.getInstance(project).reformat(psiFile)

                // Update editor with formatted text
                editor.document.setText(psiFile.text)

            } catch (e: Exception) {
                Messages.showErrorDialog("Nothing to format: JSON is empty", "Formatting Error")
                Messages.showErrorDialog("${e.message}", "Formatting Error")
            }
        }
    }

    // Save button: saves JSON to disk only when clicked; uses VirtualFile
    private val saveButton = ButtonFactory.createDefaultButton("Save", AllIcons.Actions.AddFile) {
        if (virtualFile != null) {
            runWriteAction {
                virtualFile.setBinaryContent(editor.document.text.toByteArray())
            }
            Messages.showInfoMessage("JSON saved!", "Saved")
        }
    }

    // Stringify button: converts JSON to a single-line string
    private val stringifyButton = ButtonFactory.createNormalButton("Stringify") {
        val singleLine = JsonUtils.stringifyJson(editor.document.text)
        if (singleLine != null) runWriteAction { editor.document.setText(singleLine) }
        else Messages.showErrorDialog("Invalid JSON", "Error")
    }

    // DeStringify button: converts single-line JSON back to formatted JSON
    private val deStringifyButton = ButtonFactory.createNormalButton("DeStringify") {
        val deStringify = JsonUtils.deStringifyJson(editor.document.text)
        if (deStringify != null) runWriteAction { editor.document.setText(deStringify) }
        else Messages.showErrorDialog("Invalid JSON", "Error")
    }

    // Copy button: copies current JSON to system clipboard
    private val copyButton = ButtonFactory.createNormalButton("Copy", AllIcons.Actions.Copy) {
        CopyPasteManager.getInstance().setContents(StringSelection(editor.document.text))
        Messages.showInfoMessage("JSON copied to clipboard", "Copy JSON")
    }

    // -------------------
    // Initialization
    // -------------------
    init {
        title = "JSON Box"  // Dialog title
        init()
    }

    // -------------------
    // Build UI Components
    // -------------------
    override fun createCenterPanel(): JComponent {
        // Main panel uses BorderLayout
        val panel = JBPanel<JBPanel<*>>(BorderLayout(10, 10))
        panel.add(editor.component, BorderLayout.CENTER)  // Editor in the center

        // Button panel at the bottom
        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.CENTER, 10, 10))
        buttonPanel.add(validateButton)
        buttonPanel.add(formatButton)
        buttonPanel.add(stringifyButton)
        buttonPanel.add(deStringifyButton)
        buttonPanel.add(copyButton)
        buttonPanel.add(saveButton)
        buttonPanel.add(searchButton, 0) // Place Search button first in the flow

        panel.add(buttonPanel, BorderLayout.SOUTH)
        return panel
    }

    // -------------------
    // Clean up editor resources
    // -------------------
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(editor)
        super.dispose()
    }

    // -------------------
    // Hide default OK/Cancel buttons
    // -------------------
    override fun createActions(): Array<out Action?> = arrayOf()
}