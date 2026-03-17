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
import com.intellij.openapi.application.ApplicationManager
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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.WindowConstants

/**
 * A completely independent top-level OS window for editing JSON files.
 * This can be minimized, resized, and will go behind the main IDE window
 * when losing focus, behaving exactly like a normal desktop application.
 */
class JsonBoxDialog(
    private val project: Project,
    private val virtualFile: VirtualFile?,
    private val jsonItem: JsonItem? = null,
    private val editMode: Boolean = false,
    private var onSave: ((content: JsonItem) -> Unit)? = null
) : JFrame() {

    // Create the editor instance (syntax-highlighted, folding enabled)
    private val editor: EditorEx = JsonEditorFactory.createEditor(project, virtualFile, jsonItem?.json ?: "")
    val state = project.service<JsonQuickListState>()

    // Text field for naming the JSON snippet
    private val jsonNameField = JBTextField(
        jsonItem?.title ?: generateDefaultName()
    )

    // Status label to show whether JSON is valid or invalid
    private val statusLabel: JLabel = JLabel().apply {
        foreground = UIUtil.getContextHelpForeground()
    }

    // Size label to show the size of the JSON content
    private val sizeLabel: JLabel = JLabel().apply {
        foreground = UIUtil.getContextHelpForeground()
    }

    // -------------------
    // Button Definitions
    // -------------------

    // Search button: opens the IntelliJ search overlay in the editor
    private val searchButton =
        ButtonFactory.createNormalButton(
            JsonBoxBundle.message("jsonbox.dialog.search.title"),
            AllIcons.Actions.Search,
            JsonBoxBundle.message("jsonbox.dialog.search.placeholder")
        ) {
            EditorSearchSession.start(editor, project)
        }

    // Format button: pretty-prints JSON, replaces text in editor if valid
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

        // Capture the modification stamp before running the async task
        val modificationStamp = editor.document.modificationStamp

        // Run formatting on a background thread to prevent UI freezing
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                // Try lightweight string-based formatting first for performance
                val formattedStr = JsonUtils.formatJson(text)

                ApplicationManager.getApplication().invokeLater {
                    // Check if the document was modified or if the frame was disposed
                    if (!isDisplayable || editor.document.modificationStamp != modificationStamp) return@invokeLater

                    if (formattedStr != null) {
                        // If fast formatting succeeds, update document directly
                        runWriteAction { editor.document.setText(formattedStr) }
                    } else {
                        // Fallback to heavy PSI-based formatting if necessary (e.g., partial syntax errors)
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
                }
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    if (isDisplayable) {
                        Messages.showErrorDialog(
                            "${e.message}",
                            JsonBoxBundle.message("jsonbox.dialog.format.error.title")
                        )
                    }
                }
            }
        }
    }

    private val minifyButton = ButtonFactory.createNormalButton(
        JsonBoxBundle.message("jsonbox.dialog.minify.json"),
        AllIcons.Actions.Collapseall
    ) {
        val text = editor.document.text
        if (text.isBlank()) {
            return@createNormalButton
        }

        val modificationStamp = editor.document.modificationStamp

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val minifiedStr = JsonUtils.minifyJson(text)

                ApplicationManager.getApplication().invokeLater {
                    if (!isDisplayable || editor.document.modificationStamp != modificationStamp) return@invokeLater

                    if (minifiedStr != null) {
                        runWriteAction { editor.document.setText(minifiedStr) }
                    } else {
                        Messages.showErrorDialog(
                            JsonBoxBundle.message("jsonbox.dialog.format.error.message"), // reuse formatting error message
                            JsonBoxBundle.message("jsonbox.dialog.format.error.title")
                        )
                    }
                }
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    if (isDisplayable) {
                        Messages.showErrorDialog(
                            "${e.message}",
                            JsonBoxBundle.message("jsonbox.error.title")
                        )
                    }
                }
            }
        }
    }

    // Save button: saves JSON to disk/state only when clicked
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

                if (onSave != null) {
                    // Custom callback handling
                    onSave?.invoke(newItem)
                } else {
                    // Default behavior: Access the state service directly
                    if (editMode) state.update(newItem) else state.add(newItem)
                }
                // Close the dialog after saving
                dispose()
            }
        }

    // Stringify button: converts JSON to a single-line string
    private val stringifyButton = ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.dialog.stringify")) {
        val text = editor.document.text
        val modificationStamp = editor.document.modificationStamp

        ApplicationManager.getApplication().executeOnPooledThread {
            val singleLine = JsonUtils.stringifyJson(text)
            ApplicationManager.getApplication().invokeLater {
                if (!isDisplayable || editor.document.modificationStamp != modificationStamp) return@invokeLater

                if (singleLine != null) runWriteAction { editor.document.setText(singleLine) }
                else Messages.showErrorDialog(
                    JsonBoxBundle.message("jsonbox.dialog.stringify.invalid.message"),
                    JsonBoxBundle.message("jsonbox.error.title")
                )
            }
        }
    }

    // DeStringify button: converts single-line JSON back to formatted JSON
    private val deStringifyButton =
        ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.dialog.deStringify")) {
            val text = editor.document.text
            val modificationStamp = editor.document.modificationStamp

            ApplicationManager.getApplication().executeOnPooledThread {
                val deStringify = JsonUtils.deStringifyJson(text)
                ApplicationManager.getApplication().invokeLater {
                    if (!isDisplayable || editor.document.modificationStamp != modificationStamp) return@invokeLater

                    if (deStringify != null) runWriteAction { editor.document.setText(deStringify) }
                    else Messages.showErrorDialog(
                        JsonBoxBundle.message("jsonbox.dialog.deStringify.invalid.message"),
                        JsonBoxBundle.message("jsonbox.error.title")
                    )
                }
            }
        }

    // Copy button: copies current JSON to system clipboard
    private val copyButton =
        ButtonFactory.createNormalButton(JsonBoxBundle.message("jsonbox.button.copy"), AllIcons.Actions.Copy) {
            CopyPasteManager.getInstance().setContents(StringSelection(editor.document.text))
            Messages.showInfoMessage(
                JsonBoxBundle.message("jsonbox.dialog.copy.message"),
                JsonBoxBundle.message("jsonbox.dialog.copy.title")
            )
        }

    // -------------------
    // Initialization
    // -------------------
    init {
        title = JsonBoxBundle.message("jsonbox.title")
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        jsonNameField.text =
            if (jsonNameField.text.isNullOrBlank()) generateDefaultName()
            else jsonNameField.text

        // Listen for document changes to update validity indicators in real-time
        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                updateIndicators(editor.document.text)
            }
        })

        contentPane = createCenterPanel()
        pack()

        // Ensure minimum size so buttons are not hidden
        minimumSize = Dimension(900, 600)

        // Match the IDE window's icon and center relative to the IDE on the correct monitor
        val ideFrame = WindowManager.getInstance().getFrame(project)
        if (ideFrame != null) {
            this.iconImages = ideFrame.iconImages
        }
        setLocationRelativeTo(ideFrame)

        // Initial validation check
        updateIndicators(editor.document.text)
    }

    /**
     * Overriding dispose to ensure the editor is always released,
     * especially important for unit tests.
     */
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(editor)
        super.dispose()
    }

    // -------------------
    // Build UI Components
    // -------------------
    private fun createCenterPanel(): JComponent {
        val panel = JBPanel<JBPanel<*>>(BorderLayout(10, 10))
        panel.border = JBUI.Borders.empty(10)

        // ---------- Top: JSON name ----------
        val namePanel = JBPanel<JBPanel<*>>(BorderLayout(5, 5))
        namePanel.add(JLabel(JsonBoxBundle.message("jsonbox.dialog.label.name")), BorderLayout.WEST)
        namePanel.add(jsonNameField, BorderLayout.CENTER)
        panel.add(namePanel, BorderLayout.NORTH)

        // ---------- Center: Editor and Status ----------
        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(createStatusPanel(), BorderLayout.NORTH)
        centerPanel.add(editor.component, BorderLayout.CENTER)
        panel.add(centerPanel, BorderLayout.CENTER)

        // ---------- Bottom: Buttons ----------
        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.CENTER, 10, 10))
        buttonPanel.add(formatButton)
        buttonPanel.add(minifyButton)
        buttonPanel.add(stringifyButton)
        buttonPanel.add(deStringifyButton)
        buttonPanel.add(copyButton)
        buttonPanel.add(saveButton)
        buttonPanel.add(searchButton, 0)

        // Wrap the button panel in a scroll pane just in case the window gets too narrow
        val buttonScrollPane = JBScrollPane(buttonPanel).apply {
            border = JBUI.Borders.empty()
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }

        panel.add(buttonScrollPane, BorderLayout.SOUTH)

        panel.preferredSize = Dimension(900, 600)
        return panel
    }

    /**
     * Creates the top panel containing the validity status and size labels.
     */
    private fun createStatusPanel(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            isOpaque = false
            border = JBUI.Borders.emptyBottom(4)
            add(statusLabel)
            add(sizeLabel)
        }
    }

    /**
     * Determines whether to run validation synchronously or asynchronously based on payload size.
     */
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

    // -------------------
    // UI Update Methods
    // -------------------

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
}