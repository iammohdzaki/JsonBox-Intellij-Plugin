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
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
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
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent

class JsonBoxQuickPopup(
    private val project: Project
) {

    private val state = project.service<JsonQuickListState>()

    private val allItems = mutableListOf<JsonItem>()
    private val listModel = DefaultListModel<JsonItem>()
    private val jsonList = JBList(listModel)
    
    private var popup: JBPopup? = null

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

    init {
        initList()
        initEditor()
        loadItems()
        initSearch()
    }

    private fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout(8, 8))
        root.border = JBUI.Borders.empty(8)

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

        val editorPanel = previewEditor.component.apply {
            border = JBUI.Borders.empty()
        }
        root.add(leftPanel, BorderLayout.WEST)
        root.add(editorPanel, BorderLayout.CENTER)

        root.preferredSize = Dimension(900, 550)
        return root
    }

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
                JsonBoxPopup(
                    project = project,
                    virtualFile = null,
                    jsonItem = item,
                    editMode = true
                ).show()
                popup?.cancel()
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

    private fun initList() {
        jsonList.selectionMode = ListSelectionModel.SINGLE_SELECTION
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

    private fun initSearch() {
        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                applyFilter(searchField.text)
            }
        })
        searchField.textEditor.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_DOWN && listModel.size() > 0) {
                    jsonList.requestFocus()
                    jsonList.selectedIndex = 0
                }
            }
        })
    }

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

    private fun onAdd() {
        JsonBoxPopup(project, null, jsonItem = null, onSave = { jsonItem ->
            state.add(jsonItem)
            loadItems()
            JsonBoxNotifications.notify(
                project,
                JsonBoxBundle.message("jsonbox.title"),
                JsonBoxBundle.message("jsonbox.notification.added.message")
            )
            JsonBoxQuickPopup(project).show()
        }).show()
        popup?.cancel()
    }

    private fun onEdit() {
        val item = selectedItem() ?: return
        JsonBoxPopup(project, null, jsonItem = item, editMode = true, onSave = { jsonItem ->
            state.update(jsonItem)
            loadItems()
            JsonBoxNotifications.notify(
                project,
                JsonBoxBundle.message("jsonbox.title"),
                JsonBoxBundle.message("jsonbox.notification.update.message")
            )
            JsonBoxQuickPopup(project).show()
        }).show()
        popup?.cancel()
    }

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

    fun show() {
        val panel = createCenterPanel()
        
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, searchField)
            .setTitle(JsonBoxBundle.message("jsonbox.quick.title"))
            .setMovable(true)
            .setResizable(true)
            .setRequestFocus(true)
            .setCancelOnClickOutside(false) // Changed to false
            .setCancelKeyEnabled(true)
            .setDimensionServiceKey(project, "JsonBoxQuickPopupSize", false)
            .createPopup()
            
        popup?.addListener(object : com.intellij.openapi.ui.popup.JBPopupListener {
            override fun onClosed(event: com.intellij.openapi.ui.popup.LightweightWindowEvent) {
                EditorFactory.getInstance().releaseEditor(previewEditor)
            }
        })
            
        popup?.showCenteredInCurrentWindow(project)
    }
}