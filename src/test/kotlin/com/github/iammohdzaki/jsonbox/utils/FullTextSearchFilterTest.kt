package com.github.iammohdzaki.jsonbox.utils

import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for the full-text search filter logic introduced in
 * [com.github.iammohdzaki.jsonbox.dialog.JsonBoxQuickDialog].
 *
 * The actual filter runs inside [JsonBoxQuickDialog.applyFilter], but the matching
 * rules are purely data-driven (title + JSON content). We replicate the exact
 * predicate logic here so it can be tested without a UI or platform bootstrap beyond
 * [BasePlatformTestCase].
 */
class FullTextSearchFilterTest : BasePlatformTestCase() {

    // -----------------------------------------------------------------------
    // Helpers — mirror the predicate used in applyFilter()
    // -----------------------------------------------------------------------

    private enum class MatchType { TITLE, CONTENT }

    private data class FilterResult(val item: JsonItem, val matchType: MatchType)

    /**
     * Applies the same filter logic as [JsonBoxQuickDialog.applyFilter].
     * Returns only matching items paired with how they matched.
     */
    private fun applyFilter(items: List<JsonItem>, query: String): List<FilterResult> {
        if (query.isBlank()) return items.map { FilterResult(it, MatchType.TITLE) }
        val q = query.lowercase()
        val results = mutableListOf<FilterResult>()
        for (item in items) {
            when {
                item.title.lowercase().contains(q) -> results += FilterResult(item, MatchType.TITLE)
                item.json.lowercase().contains(q)  -> results += FilterResult(item, MatchType.CONTENT)
            }
        }
        return results
    }

    // -----------------------------------------------------------------------
    // Blank / empty query — should return everything
    // -----------------------------------------------------------------------

    fun testBlankQuery_ReturnsAllItems() {
        val items = listOf(
            JsonItem(title = "Alpha", json = """{"a":1}"""),
            JsonItem(title = "Beta",  json = """{"b":2}""")
        )
        val result = applyFilter(items, "")
        assertEquals(2, result.size)
    }

    fun testWhitespaceOnlyQuery_ReturnsAllItems() {
        val items = listOf(
            JsonItem(title = "X", json = """{}"""),
            JsonItem(title = "Y", json = """{}""")
        )
        assertEquals(2, applyFilter(items, "   ").size)
    }

    // -----------------------------------------------------------------------
    // Title-only matches
    // -----------------------------------------------------------------------

    fun testTitleMatch_ReturnsCorrectItem() {
        val items = listOf(
            JsonItem(title = "Auth Token",   json = """{"scope":"read"}"""),
            JsonItem(title = "User Profile", json = """{"scope":"write"}""")
        )
        val result = applyFilter(items, "auth")
        assertEquals(1, result.size)
        assertEquals("Auth Token", result.first().item.title)
        assertEquals(MatchType.TITLE, result.first().matchType)
    }

    fun testTitleMatch_IsCaseInsensitive() {
        val items = listOf(JsonItem(title = "ProductionConfig", json = """{}"""))
        val result = applyFilter(items, "PRODUCTIONCONFIG")
        assertEquals(1, result.size)
        assertEquals(MatchType.TITLE, result.first().matchType)
    }

    fun testTitleMatch_PartialSubstring() {
        val items = listOf(
            JsonItem(title = "ShippingAddress", json = """{}"""),
            JsonItem(title = "BillingAddress",  json = """{}"""),
            JsonItem(title = "PhoneNumber",     json = """{}""")
        )
        val result = applyFilter(items, "address")
        assertEquals(2, result.size)
        assertTrue(result.all { it.matchType == MatchType.TITLE })
    }

    // -----------------------------------------------------------------------
    // Content-only matches
    // -----------------------------------------------------------------------

    fun testContentMatch_WhenTitleDoesNotMatch() {
        val items = listOf(
            JsonItem(title = "Config A", json = """{"endpoint":"https://api.prod.example.com"}"""),
            JsonItem(title = "Config B", json = """{"endpoint":"https://api.staging.example.com"}""")
        )
        val result = applyFilter(items, "prod")
        assertEquals(1, result.size)
        assertEquals("Config A", result.first().item.title)
        assertEquals(MatchType.CONTENT, result.first().matchType)
    }

    fun testContentMatch_IsCaseInsensitive() {
        val items = listOf(
            JsonItem(title = "Payload", json = """{"STATUS":"ACTIVE"}""")
        )
        val result = applyFilter(items, "status")
        assertEquals(1, result.size)
        assertEquals(MatchType.CONTENT, result.first().matchType)
    }

    fun testContentMatch_FindsNestedField() {
        val items = listOf(
            JsonItem(title = "Order", json = """{"customer":{"email":"alice@example.com"}}""")
        )
        val result = applyFilter(items, "alice@example.com")
        assertEquals(1, result.size)
        assertEquals(MatchType.CONTENT, result.first().matchType)
    }

    fun testContentMatch_FindsValueInArray() {
        val items = listOf(
            JsonItem(title = "Roles", json = """{"roles":["admin","editor","viewer"]}""")
        )
        val result = applyFilter(items, "editor")
        assertEquals(1, result.size)
        assertEquals(MatchType.CONTENT, result.first().matchType)
    }

    // -----------------------------------------------------------------------
    // Title takes priority over content when both match
    // -----------------------------------------------------------------------

    fun testTitlePriority_WhenBothTitleAndContentMatch() {
        val items = listOf(
            JsonItem(title = "prod-config", json = """{"env":"prod","region":"us-east"}""")
        )
        // "prod" matches both the title and the JSON content
        val result = applyFilter(items, "prod")
        assertEquals(1, result.size)
        assertEquals(
            "Title match must take priority over content match",
            MatchType.TITLE,
            result.first().matchType
        )
    }

    // -----------------------------------------------------------------------
    // No match
    // -----------------------------------------------------------------------

    fun testNoMatch_ReturnsEmptyList() {
        val items = listOf(
            JsonItem(title = "Foo", json = """{"x":1}"""),
            JsonItem(title = "Bar", json = """{"y":2}""")
        )
        val result = applyFilter(items, "zzz_nonexistent")
        assertTrue(result.isEmpty())
    }

    fun testNoMatch_EmptyItemsList_ReturnsEmptyList() {
        val result = applyFilter(emptyList(), "anything")
        assertTrue(result.isEmpty())
    }

    // -----------------------------------------------------------------------
    // Mixed results — some title, some content
    // -----------------------------------------------------------------------

    fun testMixedMatch_TitleAndContentItemsReturnedTogether() {
        val items = listOf(
            JsonItem(title = "payment-gateway",  json = """{"provider":"stripe"}"""),    // title match
            JsonItem(title = "notification",     json = """{"channel":"payment-sms"}"""), // content match
            JsonItem(title = "user-preferences", json = """{"theme":"dark"}""")          // no match
        )
        val result = applyFilter(items, "payment")
        assertEquals(2, result.size)
        val titleMatches   = result.filter { it.matchType == MatchType.TITLE }
        val contentMatches = result.filter { it.matchType == MatchType.CONTENT }
        assertEquals(1, titleMatches.size)
        assertEquals(1, contentMatches.size)
        assertEquals("payment-gateway",  titleMatches.first().item.title)
        assertEquals("notification",     contentMatches.first().item.title)
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    fun testSingleCharacterQuery_Works() {
        val items = listOf(
            JsonItem(title = "A-config", json = """{}"""),
            JsonItem(title = "B-config", json = """{"a":1}""")  // content match on "a"
        )
        // "a" matches title "A-config" and content {"a":1}
        val result = applyFilter(items, "a")
        assertEquals(2, result.size)
    }

    fun testSpecialCharactersInQuery_MatchedLiterally() {
        val items = listOf(
            JsonItem(title = "Special", json = """{"url":"https://api.example.com/v1"}""")
        )
        val result = applyFilter(items, "https://api.example.com")
        assertEquals(1, result.size)
        assertEquals(MatchType.CONTENT, result.first().matchType)
    }
}
