package com.github.iammohdzaki.jsonbox.components

import javax.swing.Icon
import javax.swing.JButton

/**
 * A factory for creating consistent [JButton] instances across the plugin.
 */
object ButtonFactory {
    /**
     * Creates a button with default styling, suitable for primary actions.
     *
     * @param name The text to display on the button.
     * @param icon An optional icon to display.
     * @param onClick The callback to execute when the button is clicked.
     * @return A styled [JButton].
     */
    fun createDefaultButton(name: String, icon: Icon? = null, onClick: () -> Unit): JButton {
        return JButton(name, icon).apply {
            addActionListener { onClick() }
            isDefaultCapable = true
            isFocusable = true
        }
    }

    /**
     * Creates a normal button with optional tooltip.
     *
     * @param name The text to display on the button.
     * @param icon An optional icon to display.
     * @param toolTip An optional tooltip for the button.
     * @param onClick The callback to execute when the button is clicked.
     * @return A [JButton] instance.
     */
    fun createNormalButton(
        name: String,
        icon: Icon? = null,
        toolTip: String? = null,
        onClick: () -> Unit
    ): JButton {
        return JButton(name, icon).apply {
            toolTip?.let {
                toolTipText = it
            }
            addActionListener { onClick() }
        }
    }
}