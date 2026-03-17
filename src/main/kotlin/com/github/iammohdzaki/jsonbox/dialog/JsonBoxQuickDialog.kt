package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.notification.JsonBoxNotifications
import com.github.iammohdzaki.jsonbox.persistance.JsonQuickListState
import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.github.iammohdzaki.jsonbox.utils.JsonBoxBundle
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent

/**
 * A completely independent top-level OS window for managing saved JSON snippets.
 * This features a searchable list on the left and a read-only preview editor on the right.
 */
class JsonBoxQuickDialog(
    private val project: Project
) : JFrame() {

    private val state = project.service<JsonQuickListState>()

    // ---- Left list ----
    private val allItems = mutableListOf<JsonItem>()
    private val listModel = DefaultListModel<JsonItem>()
    private val jsonList = JBList(listModel)

    // ---- Right preview editor (read-only) ----
    private val previewEditor: EditorEx =
        EditorFactory.getInstance().createEditor(
            EditorFactory.getInstance().createDocument(""),
            project,
            FileTypeManager.getInstance().getFileTypeByExtension("json"),
            true
        ) as EditorEx

    val searchField = SearchTextField().apply {
        textEditor.emptyText.text = JsonBoxBundle.message("jsonbox.search.hint")
    }

    // -------------------
    // Initialization
    // -------------------
    init {
        title = JsonBoxBundle.message("jsonbox.quick.title")
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        initList()
        initEditor()
        loadItems()
        initSearch()

        contentPane = createCenterPanel()
        pack()

        // Ensure minimum size
        minimumSize = Dimension(900, 550)

        // Match the IDE window's icon and center relative to the IDE
        val ideFrame = WindowManager.getInstance().getFrame(project)
        if (ideFrame != null) {
            this.iconImages = ideFrame.iconImages
        }
        setLocationRelativeTo(ideFrame)
    }

    /**
     * Overriding dispose to ensure the editor is always released,
     * especially important for unit tests.
     */
    override fun dispose() {
        if (!previewEditor.isDisposed) {
            EditorFactory.getInstance().releaseEditor(previewEditor)
        }
        super.dispose()
    }

    // ---------------- UI setup ----------------

    private fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout(8, 8))
        root.border = JBUI.Borders.empty(8)

        // Setup the list with Add/Edit/Delete actions
        val decoratedListPanel = ToolbarDecorator.createDecorator(jsonList)
            .disableUpDownActions()
            .setAddAction { onAdd() }
            .setEditAction { onEdit() }
            .setRemoveAction { onDelete() }
            .createPanel()

        val leftPanel = JPanel(BorderLayout(0, JBUI.scale(6))).apply {
            preferredSize = Dimension(JBUI.scale(260), 0)
            minimumSize = Dimension(JBUI.scale(220), 0)

            add(searchField, BorderLayout.NORTH)
            add(decoratedListPanel, BorderLayout.CENTER)
        }

        // Center panel (editor)
        val editorPanel = previewEditor.component.apply {
            border = JBUI.Borders.empty()
        }
        root.add(leftPanel, BorderLayout.WEST)
        root.add(editorPanel, BorderLayout.CENTER)

        root.preferredSize = Dimension(900, 550)
        return root
    }

    /**
     * Installs a custom header inside the read-only editor containing copy and open actions.
     */
    private fun installEditorHeader() {
        val scheme = EditorColorsManager.getInstance().globalScheme

        val headerPanel = JPanel(BorderLayout()).apply {
            isOpaque = true
            background = scheme.defaultBackground
            border = JBUI.Borders.empty(4, 8)
        }

        val titleLabel = JLabel(JsonBoxBundle.message("jsonbox.preview.title")).apply {
            foreground = UIUtil.getContextHelpForeground()
        }

        val actionsPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
            isOpaque = false
        }

        val copyAction = object : AnAction(
            JsonBoxBundle.message("jsonbox.button.copy"),
            JsonBoxBundle.message("jsonbox.button.copy.description"),
            AllIcons.Actions.Copy
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                onCopy()
            }
        }

        val openAction = object : AnAction(
            JsonBoxBundle.message("jsonbox.button.openFull.title"),
            JsonBoxBundle.message("jsonbox.button.openFull.description"),
            AllIcons.Actions.OpenNewTab
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                val item = selectedItem() ?: return
                JsonBoxDialog(
                    project = project,
                    virtualFile = null,
                    jsonItem = item,
                    editMode = true
                ).apply { isVisible = true }
                dispose()
            }
        }

        val copyButton = ActionButton(
            copyAction,
            Presentation(),
            "JsonBoxEditorHeader",
            ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
        )

        val openButton = ActionButton(
            openAction,
            Presentation(),
            "JsonBoxEditorHeader",
            ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
        )

        actionsPanel.add(copyButton)
        actionsPanel.add(openButton)

        headerPanel.add(titleLabel, BorderLayout.WEST)
        headerPanel.add(actionsPanel, BorderLayout.EAST)

        previewEditor.headerComponent = headerPanel
    }

    // ---------------- List ----------------

    private fun initList() {
        jsonList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        // Update the preview editor when a different item is selected
        jsonList.addListSelectionListener {
            val item = jsonList.selectedValue ?: return@addListSelectionListener
            ApplicationManager.getApplication().runWriteAction {
                previewEditor.document.setText(item.json)
            }
        }

        jsonList.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val component = SimpleColoredComponent()

                if (value != null) {
                    value as JsonItem
                    component.append(
                        value.title,
                        SimpleTextAttributes.REGULAR_ATTRIBUTES
                    )
                }

                component.border = JBUI.Borders.empty(6, 10)
                component.isOpaque = true

                if (isSelected) {
                    component.background = list.selectionBackground
                    component.foreground = list.selectionForeground
                } else {
                    component.background = list.background
                    component.foreground = list.foreground
                }

                return component
            }
        }
    }

    private fun loadItems() {
        allItems.clear()
        allItems.addAll(state.items)
        applyFilter("")
    }

    // ---------------- Editor ----------------

    private fun initEditor() {
        previewEditor.settings.isLineNumbersShown = true
        previewEditor.settings.isFoldingOutlineShown = true
        val scheme = EditorColorsManager.getInstance().globalScheme
        previewEditor.colorsScheme = scheme
        previewEditor.backgroundColor = scheme.defaultBackground
        val syntaxHighlighter =
            SyntaxHighlighterFactory.getSyntaxHighlighter(JsonLanguage.INSTANCE, project, null)
        previewEditor.highlighter =
            com.intellij.openapi.editor.ex.util.LexerEditorHighlighter(syntaxHighlighter, scheme)

        installEditorHeader()
    }

    // ---------------- Search ----------------
    private fun initSearch() {
        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                applyFilter(searchField.text)
            }
        })
        searchField.textEditor.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                // Focus list on down arrow key press
                if (e.keyCode == KeyEvent.VK_DOWN && listModel.size() > 0) {
                    jsonList.requestFocus()
                    jsonList.selectedIndex = 0
                }
            }
        })
    }

    // ---------------- Actions ----------------

    /**
     * Filters the list of JSON snippets based on the search query.
     */
    private fun applyFilter(query: String) {
        listModel.clear()

        val filteredItems =
            if (query.isBlank()) {
                allItems
            } else {
                val q = query.lowercase()
                allItems.filter {
                    it.title.lowercase().contains(q)
                }
            }

        filteredItems.forEach { listModel.addElement(it) }

        if (!listModel.isEmpty) {
            jsonList.selectedIndex = 0
        } else {
            ApplicationManager.getApplication().runWriteAction {
                previewEditor.document.setText("")
            }
        }
    }

    private fun selectedItem(): JsonItem? = jsonList.selectedValue

    /**
     * Opens the editor dialog in 'Add' mode.
     */
    private fun onAdd() {
        JsonBoxDialog(project, null, jsonItem = null, onSave = { jsonItem ->
            state.add(jsonItem)
            loadItems()
            JsonBoxNotifications.notify(
                project,
                JsonBoxBundle.message("jsonbox.title"),
                JsonBoxBundle.message("jsonbox.notification.added.message")
            )
            JsonBoxQuickDialog(project).apply { isVisible = true }
        }).apply { isVisible = true }
        dispose()
    }

    /**
     * Opens the editor dialog in 'Edit' mode for the selected item.
     */
    private fun onEdit() {
        val item = selectedItem() ?: return
        JsonBoxDialog(project, null, jsonItem = item, editMode = true, onSave = { jsonItem ->
            state.update(jsonItem)
            loadItems()
            JsonBoxNotifications.notify(
                project,
                JsonBoxBundle.message("jsonbox.title"),
                JsonBoxBundle.message("jsonbox.notification.update.message")
            )
            JsonBoxQuickDialog(project).apply { isVisible = true }
        }).apply { isVisible = true }
        dispose()
    }

    /**
     * Prompts for confirmation and deletes the selected item.
     */
    private fun onDelete() {
        val item = selectedItem() ?: return
        val confirm = Messages.showYesNoDialog(
            project,
            JsonBoxBundle.message("jsonbox.confirm.delete", item.title),
            JsonBoxBundle.message("jsonbox.confirm.delete.title"),
            Messages.getWarningIcon()
        )
        if (confirm == Messages.YES) {
            state.delete(item.id)
            loadItems()
            JsonBoxNotifications.notify(
                project,
                JsonBoxBundle.message("jsonbox.title"),
                JsonBoxBundle.message("jsonbox.notification.delete.message")
            )
        }
    }

    /**
     * Copies the selected JSON to the system clipboard.
     */
    private fun onCopy() {
        val item = selectedItem() ?: return
        CopyPasteManager.getInstance()
            .setContents(StringSelection(item.json))
        JsonBoxNotifications.notify(
            project,
            JsonBoxBundle.message("jsonbox.dialog.copy.message"),
            JsonBoxBundle.message("jsonbox.dialog.copy.title")
        )
    }
}