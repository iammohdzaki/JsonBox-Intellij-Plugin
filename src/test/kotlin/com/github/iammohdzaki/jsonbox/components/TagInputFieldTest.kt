package com.github.iammohdzaki.jsonbox.components

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.GraphicsEnvironment

class TagInputFieldTest : BasePlatformTestCase() {

    fun testTagInputField_initialTags() {
        if (GraphicsEnvironment.isHeadless()) return
        val tagField = TagInputField(listOf("prod", "api"))
        
        val tags = tagField.getTags()
        assertEquals(2, tags.size)
        assertTrue(tags.contains("prod"))
        assertTrue(tags.contains("api"))
    }

    fun testTagInputField_commitTag() {
        if (GraphicsEnvironment.isHeadless()) return
        val tagField = TagInputField()
        
        tagField.inputField.text = "dev,"
        tagField.commitTag()
        
        val tags = tagField.getTags()
        assertEquals(1, tags.size)
        assertEquals("dev", tags[0])
        assertEquals("", tagField.inputField.text)
    }

    fun testTagInputField_duplicateTagsIgnored() {
        if (GraphicsEnvironment.isHeadless()) return
        val tagField = TagInputField(listOf("prod"))
        
        tagField.inputField.text = "prod"
        tagField.commitTag() // Should not add duplicate
        
        val tags = tagField.getTags()
        assertEquals(1, tags.size)
    }

    fun testTagInputField_uncommittedTextIsCommittedOnGetTags() {
        if (GraphicsEnvironment.isHeadless()) return
        val tagField = TagInputField()
        
        tagField.inputField.text = "typing"
        // Not calling commitTag manually
        
        val tags = tagField.getTags() // Should commit automatically
        assertEquals(1, tags.size)
        assertEquals("typing", tags[0])
    }

    fun testTagInputField_commitMultipleTags() {
        if (GraphicsEnvironment.isHeadless()) return
        val tagField = TagInputField()
        
        tagField.inputField.text = "prod, api, dev"
        tagField.commitTag()
        
        val tags = tagField.getTags()
        assertEquals(3, tags.size)
        assertEquals("prod", tags[0])
        assertEquals("api", tags[1])
        assertEquals("dev", tags[2])
    }

    fun testTagInputField_suggestionDownAndEnter() {
        if (GraphicsEnvironment.isHeadless()) return
        val availableTags = setOf("production", "api-server")
        val tagField = TagInputField(availableTags = availableTags)
        
        tagField.inputField.text = "dev, produ"
        
        // Trigger showSuggestions via reflection (catch exception thrown by JPopupMenu.show in headless mode)
        val method = TagInputField::class.java.getDeclaredMethod("showSuggestions")
        method.isAccessible = true
        try {
            method.invoke(tagField)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            if (e.cause !is java.awt.IllegalComponentStateException) throw e
        }
        
        // Get the suggestionMenu
        val menuField = TagInputField::class.java.getDeclaredField("suggestionMenu")
        menuField.isAccessible = true
        val menu = menuField.get(tagField) as javax.swing.JPopupMenu
        
        assertEquals(1, menu.componentCount)
        val menuItem = menu.getComponent(0) as javax.swing.JMenuItem
        assertEquals("production", menuItem.text)
        
        // Simulate pressing Enter on the menu item
        menuItem.doClick()
        
        val tags = tagField.getTags()
        assertEquals(2, tags.size)
        assertEquals("dev", tags[0])
        assertEquals("production", tags[1])
    }
}
