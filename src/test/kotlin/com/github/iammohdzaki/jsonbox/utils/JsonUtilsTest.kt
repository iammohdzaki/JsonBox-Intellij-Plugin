package com.github.iammohdzaki.jsonbox.utils

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.system.measureTimeMillis

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

    fun testFormatJson_Valid() {
        val unformatted = """{"key":"value","number":123}"""
        val expected = """{
  "key": "value",
  "number": 123
}"""
        val formatted = JsonUtils.formatJson(unformatted)
        assertEquals(expected, formatted?.replace("\r\n", "\n"))
    }

    fun testFormatJson_InvalidReturnsNull() {
        val invalidJson = """{"key":"value""""
        val formatted = JsonUtils.formatJson(invalidJson)
        assertNull("Invalid JSON should return null so it can fallback to PSI formatting", formatted)
    }

    fun testFormatJson_HeavyPerformance() {
        // Generate a reasonably large JSON array
        val item = """{"id": "uuid-1234-5678", "name": "Test Item", "isActive": true, "tags": ["tag1", "tag2", "tag3"], "metadata": {"created": 1234567890, "author": "John Doe"}}"""
        val largeJsonBuilder = StringBuilder("[\n")
        val itemsCount = 10_000 // Creates a JSON string of roughly 1.5MB
        
        for (i in 0 until itemsCount) {
            largeJsonBuilder.append(item)
            if (i < itemsCount - 1) largeJsonBuilder.append(",\n")
        }
        largeJsonBuilder.append("\n]")
        
        val largeJson = largeJsonBuilder.toString()
        
        // Ensure it validates correctly before timing formatting
        assertNull("The generated heavy JSON should be valid", JsonUtils.validateJson(largeJson))

        val timeTaken = measureTimeMillis {
            val formatted = JsonUtils.formatJson(largeJson)
            assertNotNull("Formatting of heavy JSON should succeed", formatted)
        }

        // The Gson format approach should be significantly faster than the PSI approach.
        // Usually, 1.5MB Gson formatting takes < 500ms on a decent machine, whereas PSI can take seconds.
        // We assert it takes less than 2 seconds to prevent long hangs.
        assertTrue("Formatting took too long: ${timeTaken}ms", timeTaken < 2000)
        
        println("Heavy JSON formatting (approx 1.5MB) took: ${timeTaken}ms")
    }
}