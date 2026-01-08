package com.github.iammohdzaki.jsonbox.utils

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JsonUtilsTest : BasePlatformTestCase() {

    fun testValidateJson_Valid() {
        val validJson = """{"key": "value"}"""
        assertNull(JsonUtils.validateJson(validJson))
    }

    fun testValidateJson_Invalid() {
        val invalidJson = """{"key": "value"""
        assertNotNull(JsonUtils.validateJson(invalidJson))
    }

    fun testValidateJson_Empty() {
        assertNotNull(JsonUtils.validateJson(""))
        assertNotNull(JsonUtils.validateJson(null))
    }

    fun testStringifyJson() {
        val json = """{"key":"value"}"""
        val stringified = JsonUtils.stringifyJson(json)
        assertEquals("\"{\\\"key\\\":\\\"value\\\"}\"", stringified)
    }

    fun testDeStringifyJson() {
        val stringified = "\"{\\\"key\\\":\\\"value\\\"}\""
        val original = JsonUtils.deStringifyJson(stringified)
        assertEquals("""{"key":"value"}""", original)
    }

    fun testIsAlreadyStringified() {
        val json = """{"key":"value"}"""
        val stringified = JsonUtils.stringifyJson(json)
        // If it's already stringified, it should return the same
        assertEquals(stringified, JsonUtils.stringifyJson(stringified))
    }
}
