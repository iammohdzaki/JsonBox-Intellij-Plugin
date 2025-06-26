package com.github.iammohdzaki.jsonbox.dialogs

import com.github.iammohdzaki.jsonbox.utils.JsonUtils
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import javax.swing.JButton
import javax.swing.JComponent

// Custom IntelliJ Dialog for JSON Parsing
class JsonParserDialog(private val initialText: String = "") : DialogWrapper(true) {

    private val textArea = JBTextArea(20, 50)

    private val validateButton = JButton("Validate")

    private val formatButton = JButton("Format")

    private val copyButton = JButton("Copy JSON")

    private val copySingleLineButton = JButton("Copy as Single Line")

    init {
        title = "JSON Parser"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JBPanel<JBPanel<*>>(BorderLayout(10, 10))
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.font = Font("Monospaced", Font.PLAIN, 14)

        val scrollPane = JBScrollPane(textArea)
        scrollPane.preferredSize = Dimension(600, 400)
        panel.add(scrollPane, BorderLayout.CENTER)
        textArea.text = initialText
        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.CENTER, 10, 10))
        buttonPanel.add(validateButton)
        buttonPanel.add(formatButton)
        buttonPanel.add(copyButton)
        buttonPanel.add(copySingleLineButton)

        panel.add(buttonPanel, BorderLayout.SOUTH)


        validateButton.addActionListener {
            val isValid = JsonUtils.validateJson(textArea.text)
            Messages.showInfoMessage(
                if (isValid) "Valid JSON" else "Invalid JSON",
                "JSON Validation"
            )
        }

        formatButton.addActionListener {
            val formattedJson = JsonUtils.formatJson(textArea.text)
            if (formattedJson != null) {
                textArea.text = formattedJson
            } else {
                Messages.showErrorDialog("Invalid JSON", "Error")
            }
        }

        copyButton.addActionListener {
            CopyPasteManager.getInstance().setContents(StringSelection(textArea.text))
            Messages.showInfoMessage("JSON copied to clipboard", "Copy JSON")
        }

        copySingleLineButton.addActionListener {
            val singleLineJson = JsonUtils.toSingleLineJson(textArea.text)
            if (singleLineJson != null) {
                CopyPasteManager.getInstance().setContents(StringSelection(singleLineJson))
                Messages.showInfoMessage("Single-line JSON copied to clipboard", "Copy JSON")
            } else {
                Messages.showErrorDialog("Invalid JSON", "Error")
            }
        }

        return panel
    }
}