package com.github.iammohdzaki.jsonbox.components

import javax.swing.Icon
import javax.swing.JButton

object ButtonFactory {
    fun createDefaultButton(name: String, icon: Icon? = null, onClick: () -> Unit): JButton {
        return JButton(name, icon).apply {
            addActionListener { onClick() }
            isDefaultCapable = true
            isFocusable = true
        }
    }

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