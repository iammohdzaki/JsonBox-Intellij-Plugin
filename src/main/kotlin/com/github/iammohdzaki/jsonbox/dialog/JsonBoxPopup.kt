package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.components.ButtonFactory
import com.github.iammohdzaki.jsonbox.editor.JsonEditorFactory
import com.github.iammohdzaki.jsonbox.persistance.JsonQuickListState
import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.github.iammohdzaki.jsonbox.utils.JsonBoxBundle
import com.github.iammohdzaki.jsonbox.utils.JsonIndicatorUtil
import com.github.iammohdzaki.jsonbox.utils.JsonUtils
import com.github.iammohdzaki.jsonbox.utils.UiAsync
import com.github.iammohdzaki.jsonbox.utils.Utils.generateDefaultName
import com.github.iammohdzaki.jsonbox.utils.ValidationResult
import com.intellij.find.EditorSearchSession
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * A custom popup window for editing JSON files or arbitrary JSON text in IntelliJ.
 */
class JsonBoxPopup(
    private val project: Project,
    private val virtualFile: VirtualFile?,          // Null if opened without a file
    private val jsonItem: JsonItem? = null,
    private val editMode: Boolean = false,
    private var onSave: ((content: JsonItem) -> Unit)? = null
) {

    // Create the editor instance (syntax-highlighted, folding enabled)
    private val editor: EditorEx = JsonEditorFactory.createEditor(project, virtualFile, jsonItem?.json ?: "")
    val state = project.service<JsonQuickListState>()

    private val jsonNameField = JBTextField(
        jsonItem?.title ?: generateDefaultName()
    )

    private val statusLabel: JLabel = JLabel().apply {
        foreground = UIUtil.getContextHelpForeground()
    }
    
    private val sizeLabel: JLabel = JLabel().apply {
        foreground = UIUtil.getContextHelpForeground()
    }
    
    private var popup: JBPopup? = null

    // -------------------
    // Button Definitions
    // -------------------

    private val searchButton =
        ButtonFactory.createNormalButton(
            JsonBoxBundle.message("jsonbox.dialog.search.title"),
            AllIcons.Actions.Search,
            JsonBoxBundle.message("jsonbox.dialog.search.placeholder")
        ) {
            EditorSearchSession.start(editor, project)
        }

    private val validateButton = ButtonFactory.createNormalButton(
        JsonBoxBundle.message("jsonbox.dialog.validate.title"),
        AllIcons.Actions.Checked
    ) {
        val error = JsonUtils.validateJson(editor.document.text)
        if (error == null) {
            Messages.showInfoMessage(
                JsonBoxBundle.message("jsonbox.dialog.validate.valid.message"),
                JsonBoxBundle.message("jsonbox.dialog.validate.subtitle")
            )
        } else {
            Messages.showInfoMessage(
                JsonBoxBundle.message("jsonbox.dialog.validate.invalid.message", error),
                JsonBoxBundle.message("jsonbox.dialog.validate.subtitle")
            )
        }
    }

    private val formatButton = ButtonFactory.createNormalButton(
        JsonBoxBundle.message("jsonbox.dialog.format.title"),
        AllIcons.Actions.ChangeView
    ) {
        val text = editor.document.text
        if (text.isBlank()) {
            Messages.showErrorDialog(
                JsonBoxBundle.message("jsonbox.dialog.format.error.message"),
                JsonBoxBundle.message("jsonbox.dialog.format.error.title")
            )
            return@createNormalButton
        }

        WriteCommandAction.runWriteCommandAction(project) {
            try {
                val psiFile = if (editor.virtualFile != null) {
                    PsiManager.getInstance(project).findFile(editor.virtualFile!!)!!
                } else {
                    PsiFileFactory.getInstance(project)
                        .createFileFromText("temp.json", JsonLanguage.INSTANCE, text)
                }

                CodeStyleManager.getInstance(project).reformat(psiFile)
                editor.document.setText(psiFile.text)

            } catch (e: Exception) {
                Messages.showErrorDialog(
                    "${e.message}",
                    JsonBoxBundle.message("jsonbox.dialog.format.error.title")
                )
            }
        }
    }

    private val saveButton =
        ButtonFactory.createDefaultButton(
            JsonBoxBundle.message("jsonbox.dialog.button.save.quick"),
            AllIcons.Debugger.ThreadStates.Idle
        ) {
            val content = editor.document.text
            if (jsonNameField.text.isNotEmpty() && content.isNotEmpty()) {
                val newItem = if (editMode && jsonItem != null) {
                    jsonItem.copy(json = content, title = jsonNameField.text)
                } else {
                    JsonItem(title = jsonNameField.text, json = content)
                }
                
                if (editMode) {
                    state.update(newItem)
                } else {
                    state.add(newItem)
                }
                
                if (onSave != null) {
                    onSave?.invoke(newItem)
                } else {
                    JsonBoxQuickPopup(project).show()
                }
                popup?.cancel()
            }
        }

    private val stringifyButton = ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.dialog.stringify")) {
        val singleLine = JsonUtils.stringifyJson(editor.document.text)
        if (singleLine != null) runWriteAction { editor.document.setText(singleLine) }
        else Messages.showErrorDialog(
            JsonBoxBundle.message("jsonbox.dialog.stringify.invalid.message"),
            JsonBoxBundle.message("jsonbox.error.title")
        )
    }

    private val deStringifyButton =
        ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.dialog.deStringify")) {
            val deStringify = JsonUtils.deStringifyJson(editor.document.text)
            if (deStringify != null) runWriteAction { editor.document.setText(deStringify) }
            else Messages.showErrorDialog(
                JsonBoxBundle.message("jsonbox.dialog.deStringify.invalid.message"),
                JsonBoxBundle.message("jsonbox.error.title")
            )
        }

    private val copyButton =
        ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.button.copy"), AllIcons.Actions.Copy) {
            CopyPasteManager.getInstance().setContents(StringSelection(editor.document.text))
            Messages.showInfoMessage(
                JsonBoxBundle.message("jsonbox.dialog.copy.message"),
                JsonBoxBundle.message("jsonbox.dialog.copy.title")
            )
        }

    private val compareButton =
        ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.dialog.compare.json"), AllIcons.Actions.Diff) {
            val content = editor.document.text
            if (content.isNotEmpty()) showEditableJsonCompareDialog(project, content)
        }

    init {
        jsonNameField.text =
            if (jsonNameField.text.isNullOrBlank()) generateDefaultName()
            else jsonNameField.text

        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                updateIndicators(editor.document.text)
            }
        })
        updateIndicators(editor.document.text)
    }

    private fun createCenterPanel(): JComponent {
        val panel = JBPanel<JBPanel<*>>(BorderLayout(10, 10))
        panel.border = JBUI.Borders.empty(10)
        
        val namePanel = JBPanel<JBPanel<*>>(BorderLayout(5, 5))
        namePanel.add(JLabel(JsonBoxBundle.message("jsonbox.dialog.label.name")), BorderLayout.WEST)
        namePanel.add(jsonNameField, BorderLayout.CENTER)
        panel.add(namePanel, BorderLayout.NORTH)

        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(createStatusPanel(), BorderLayout.NORTH)
        centerPanel.add(editor.component, BorderLayout.CENTER)
        panel.add(centerPanel, BorderLayout.CENTER)

        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.CENTER, 10, 10))
        buttonPanel.add(validateButton)
        buttonPanel.add(formatButton)
        buttonPanel.add(compareButton)
        buttonPanel.add(stringifyButton)
        buttonPanel.add(deStringifyButton)
        buttonPanel.add(copyButton)
        buttonPanel.add(saveButton)
        buttonPanel.add(searchButton, 0)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        panel.preferredSize = Dimension(900, 600)
        return panel
    }

    private fun createStatusPanel(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            isOpaque = false
            border = JBUI.Borders.emptyBottom(4)
            add(statusLabel)
            add(sizeLabel)
        }
    }

    private fun updateIndicators(json: String) {
        if (json.isBlank()) {
            showEmpty(json)
            return
        }
        if (JsonIndicatorUtil.isLarge(json)) {
            validateAsync(json)
        } else {
            validateInline(json)
        }
    }

    private fun validateInline(json: String) {
        when (val result = JsonIndicatorUtil.validate(json)) {
            ValidationResult.Valid -> showValid(json)
            ValidationResult.Empty -> showEmpty(json)
            is ValidationResult.Invalid -> showInvalid(json)
        }
    }

    private fun validateAsync(json: String) {
        UiAsync.runBackground(
            task = { JsonIndicatorUtil.validate(json) },
            ui = { result ->
                when (result) {
                    ValidationResult.Valid -> showValid(json)
                    ValidationResult.Empty -> showEmpty(json)
                    is ValidationResult.Invalid -> showInvalid(json)
                }
            }
        )
    }

    private fun showValid(json: String) {
        statusLabel.text = JsonBoxBundle.message("jsonbox.preview.valid")
        statusLabel.foreground = UIUtil.getLabelSuccessForeground()
        sizeLabel.text = " | ${JsonIndicatorUtil.formatSize(json)}"
    }

    private fun showEmpty(json: String) {
        statusLabel.text = JsonBoxBundle.message("jsonbox.preview.empty")
        statusLabel.foreground = UIUtil.getErrorForeground()
        sizeLabel.text = " | ${JsonIndicatorUtil.formatSize(json)}"
    }

    private fun showInvalid(json: String) {
        statusLabel.text = JsonBoxBundle.message("jsonbox.preview.invalid")
        statusLabel.foreground = UIUtil.getErrorForeground()
        sizeLabel.text = " | ${JsonIndicatorUtil.formatSize(json)}"
    }

    fun show() {
        val panel = createCenterPanel()
        
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, editor.contentComponent)
            .setTitle(JsonBoxBundle.message("jsonbox.title"))
            .setMovable(true)
            .setResizable(true)
            .setRequestFocus(true)
            .setCancelOnClickOutside(false) // Changed to false
            .setCancelKeyEnabled(true)
            .setDimensionServiceKey(project, "JsonBoxPopupSize", false)
            .createPopup()
            
        // Clean up editor when popup is closed
        popup?.addListener(object : com.intellij.openapi.ui.popup.JBPopupListener {
            override fun onClosed(event: com.intellij.openapi.ui.popup.LightweightWindowEvent) {
                EditorFactory.getInstance().releaseEditor(editor)
            }
        })
            
        popup?.showCenteredInCurrentWindow(project)
    }
}