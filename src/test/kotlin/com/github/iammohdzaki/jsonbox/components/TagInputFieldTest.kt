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
}
