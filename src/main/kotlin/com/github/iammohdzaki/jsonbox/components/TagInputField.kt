package com.github.iammohdzaki.jsonbox.components

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.border.EmptyBorder

/**
 * A custom Swing component that acts as a text input for tags.
 * When a user types a comma or presses Enter, the text is converted into a visual "pill".
 */
class TagInputField(
    initialTags: List<String> = emptyList(),
    private val availableTags: Set<String> = emptySet()
) : JPanel() {
    private val tags = mutableListOf<String>()
    private val suggestionMenu = JPopupMenu()
    
    val inputField = JBTextField().apply {
        border = JBUI.Borders.empty()
        isOpaque = false
        // Adjust width dynamically based on content, but keep a minimum width
        columns = 10
    }
    
    private val tagsPanel = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(4), JBUI.scale(2))).apply {
        isOpaque = false
    }

    var hintText: String = ""
        set(value) {
            field = value
            updateHint()
        }

    private val pillsContainer = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(4), JBUI.scale(2))).apply {
        isOpaque = false
    }

    init {
        layout = BorderLayout()
        border = JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1)
        background = UIUtil.getTextFieldBackground()
        isOpaque = true

        add(tagsPanel, BorderLayout.CENTER)
        
        tagsPanel.layout = BorderLayout()
        tagsPanel.add(pillsContainer, BorderLayout.WEST)
        tagsPanel.add(inputField, BorderLayout.CENTER)
        
        // Populate initial tags if editing
        initialTags.forEach { addTag(it) }

        inputField.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyChar == ',') {
                    e.consume()
                    commitTag()
                }
            }
            
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_DOWN && suggestionMenu.isVisible) {
                    suggestionMenu.requestFocusInWindow()
                    val firstItem = suggestionMenu.components.firstOrNull() as? JMenuItem
                    if (firstItem != null) {
                        MenuSelectionManager.defaultManager().selectedPath = arrayOf(suggestionMenu, firstItem)
                    }
                    e.consume()
                    return
                }
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    e.consume()
                    commitTag()
                } else if (e.keyCode == KeyEvent.VK_BACK_SPACE && inputField.text.isEmpty() && tags.isNotEmpty()) {
                    e.consume()
                    removeTag(tags.last())
                }
            }
        })
        
        inputField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                SwingUtilities.invokeLater { showSuggestions() }
            }
            override fun removeUpdate(e: DocumentEvent) {
                SwingUtilities.invokeLater { showSuggestions() }
            }
            override fun changedUpdate(e: DocumentEvent) {}
        })
        
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                inputField.requestFocusInWindow()
            }
        })
        
        refreshUI()
    }

    private fun showSuggestions() {
        val text = inputField.text.split(',').last().trim().lowercase()
        suggestionMenu.removeAll()
        if (text.isEmpty() || availableTags.isEmpty()) {
            suggestionMenu.isVisible = false
            return
        }
        val matches = availableTags.filter { it.lowercase().startsWith(text) && !tags.contains(it) }
        if (matches.isEmpty()) {
            suggestionMenu.isVisible = false
            return
        }
        
        matches.take(6).forEach { match ->
            val item = JMenuItem(match)
            item.addActionListener {
                val parts = inputField.text.split(',')
                parts.dropLast(1)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach(::addTag)
                
                addTag(match)
                inputField.text = ""
                suggestionMenu.isVisible = false
            }
            suggestionMenu.add(item)
        }
        
        if (!suggestionMenu.isVisible) {
            suggestionMenu.show(inputField, 0, inputField.height)
        } else {
            suggestionMenu.pack()
        }
        inputField.requestFocusInWindow()
    }

    private fun updateHint() {
        if (tags.isEmpty()) {
            inputField.emptyText.text = hintText
            inputField.isEditable = true
        } else {
            inputField.emptyText.text = ""
            inputField.isEditable = true
        }
    }

    fun commitTag() {
        inputField.text
            .split(',')
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach(::addTag)
        inputField.text = ""
    }

    private fun addTag(tag: String) {
        if (!tags.contains(tag)) {
            tags.add(tag)
            refreshUI()
        }
    }

    private fun removeTag(tag: String) {
        tags.remove(tag)
        refreshUI()
    }

    private fun refreshUI() {
        val hadFocus = inputField.hasFocus()
        pillsContainer.removeAll()
        tags.forEach { tag ->
            pillsContainer.add(createPill(tag))
        }
        
        updateHint()
        
        tagsPanel.revalidate()
        tagsPanel.repaint()
        
        if (hadFocus) {
            SwingUtilities.invokeLater {
                inputField.requestFocusInWindow()
            }
        }
    }

    /**
     * Creates a single visual pill component for a tag.
     */
    private fun createPill(tag: String): JPanel {
        val panel = JPanel(BorderLayout(JBUI.scale(6), 0))
        panel.isOpaque = true
        // Distinguish the pill background from the text field background
        panel.background = UIUtil.getPanelBackground() 
        panel.border = BorderFactory.createCompoundBorder(
            JBUI.Borders.customLine(com.intellij.ui.JBColor.border(), 1),
            EmptyBorder(JBUI.scale(2), JBUI.scale(6), JBUI.scale(2), JBUI.scale(6))
        )
        
        val label = JLabel(tag).apply {
            foreground = UIUtil.getLabelForeground()
        }
        
        val removeButton = JLabel("×").apply {
            foreground = UIUtil.getContextHelpForeground()
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = "Remove tag"
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    removeTag(tag)
                }
                override fun mouseEntered(e: MouseEvent) {
                    foreground = UIUtil.getErrorForeground()
                }
                override fun mouseExited(e: MouseEvent) {
                    foreground = UIUtil.getContextHelpForeground()
                }
            })
        }
        
        panel.add(label, BorderLayout.CENTER)
        panel.add(removeButton, BorderLayout.EAST)
        return panel
    }

    /**
     * Returns the list of parsed tags. Ensures any half-typed tag is committed first.
     */
    fun getTags(): List<String> {
        commitTag()
        return tags.toList()
    }
}
