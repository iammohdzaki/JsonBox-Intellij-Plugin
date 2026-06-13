package com.github.iammohdzaki.jsonbox.utils

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for clipboard auto-detect logic.
 *
 * The clipboard prefill in [com.github.iammohdzaki.jsonbox.dialog.JsonBoxDialog]
 * relies entirely on [JsonUtils.validateJson] and [JsonUtils.formatJson].
 * These tests verify that the contract those two functions expose is correct,
 * so the feature behaves as expected without spinning up a full UI.
 */
class ClipboardPrefillLogicTest : BasePlatformTestCase() {

    // -----------------------------------------------------------------------
    // validateJson — the gate that decides whether to prefill
    // -----------------------------------------------------------------------

    fun testValidJson_ShouldTriggerPrefill() {
        // validateJson returning null means "valid" → editor should be prefilled
        val json = """{"host":"api.example.com","port":8080}"""
        assertNull(
            "Valid JSON should return null so the clipboard prefill triggers",
            JsonUtils.validateJson(json)
        )
    }

    fun testInvalidJson_ShouldNotTriggerPrefill() {
        // validateJson returning non-null means "invalid" → prefill must be skipped
        val notJson = "Hello, World!"
        assertNotNull(
            "Plain text is not JSON; prefill must not trigger",
            JsonUtils.validateJson(notJson)
        )
    }

    fun testPartialJson_ShouldNotTriggerPrefill() {
        val partial = """{"name":"Alice"""" // missing closing brace
        assertNotNull(
            "Partial JSON must not trigger prefill",
            JsonUtils.validateJson(partial)
        )
    }

    fun testEmptyClipboard_ShouldNotTriggerPrefill() {
        assertNotNull(
            "Empty string must not trigger prefill",
            JsonUtils.validateJson("")
        )
    }

    fun testBlankClipboard_ShouldNotTriggerPrefill() {
        assertNotNull(
            "Blank/whitespace string must not trigger prefill",
            JsonUtils.validateJson("   \t\n")
        )
    }

    fun testJsonArray_ShouldTriggerPrefill() {
        val json = """[{"id":1},{"id":2}]"""
        assertNull(
            "A JSON array is valid JSON and must trigger prefill",
            JsonUtils.validateJson(json)
        )
    }

    // -----------------------------------------------------------------------
    // formatJson — called to pretty-print before inserting into the editor
    // -----------------------------------------------------------------------

    fun testPrefillFormatsCompactJson() {
        val compact = """{"a":1,"b":true}"""
        val formatted = JsonUtils.formatJson(compact)
        assertNotNull("formatJson must succeed for valid JSON", formatted)
        assertTrue(
            "Pre-filled content should be pretty-printed (contain newlines)",
            formatted!!.contains("\n")
        )
    }

    fun testPrefillFallsBackToRawWhenFormatFails() {
        // If formatJson returns null (shouldn't happen for valid JSON, but just in case),
        // the dialog falls back to the raw clip text. Verify the null path exists.
        val result = JsonUtils.formatJson("{bad}")
        assertNull("formatJson must return null for invalid JSON so the fallback path is reachable", result)
    }

    fun testPrefillKeepsAllFields() {
        val json = """{"username":"bob","token":"abc123","expires":9999}"""
        val formatted = JsonUtils.formatJson(json)
        assertNotNull(formatted)
        assertTrue(formatted!!.contains("\"username\""))
        assertTrue(formatted.contains("\"bob\""))
        assertTrue(formatted.contains("\"token\""))
        assertTrue(formatted.contains("\"abc123\""))
        assertTrue(formatted.contains("\"expires\""))
        assertTrue(formatted.contains("9999"))
    }

    fun testPrefillHandlesUnicodeJson() {
        val json = """{"greeting":"こんにちは","emoji":"😀"}"""
        assertNull("Unicode JSON must be detected as valid", JsonUtils.validateJson(json))
        val formatted = JsonUtils.formatJson(json)
        assertNotNull(formatted)
        assertTrue(formatted!!.contains("こんにちは"))
        assertTrue(formatted.contains("😀"))
    }

    fun testPrefillHandlesNestedJson() {
        val json = """{"user":{"name":"Alice","address":{"city":"Paris"}}}"""
        assertNull(JsonUtils.validateJson(json))
        val formatted = JsonUtils.formatJson(json)
        assertNotNull(formatted)
        assertTrue(formatted!!.contains("\"city\""))
        assertTrue(formatted.contains("\"Paris\""))
    }
}
