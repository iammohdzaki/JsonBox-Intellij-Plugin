package com.github.iammohdzaki.jsonbox.persistance

import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for [JsonQuickListState] — the persistence layer that backs
 * both the Quick List and the full-text search feature.
 *
 * Full-text search in [com.github.iammohdzaki.jsonbox.dialog.JsonBoxQuickDialog]
 * reads directly from [JsonQuickListState.items], so correctness of add/update/delete
 * directly affects what gets searched.
 */
class JsonQuickListStateTest : BasePlatformTestCase() {

    private lateinit var state: JsonQuickListState

    override fun setUp() {
        super.setUp()
        state = JsonQuickListState()
    }

    // -----------------------------------------------------------------------
    // add
    // -----------------------------------------------------------------------

    fun testAdd_InsertsItemIntoList() {
        val item = JsonItem(title = "Auth Token", json = """{"token":"abc"}""")
        state.add(item)
        assertEquals(1, state.items.size)
        assertEquals("Auth Token", state.items.first().title)
    }

    fun testAdd_MultipleItems_AllPresent() {
        state.add(JsonItem(title = "Item A", json = """{"a":1}"""))
        state.add(JsonItem(title = "Item B", json = """{"b":2}"""))
        state.add(JsonItem(title = "Item C", json = """{"c":3}"""))
        assertEquals(3, state.items.size)
    }

    fun testAdd_SameId_ReplacesExisting() {
        val id = "fixed-id"
        val original = JsonItem(id = id, title = "Original", json = """{"v":1}""")
        val updated = JsonItem(id = id, title = "Replaced", json = """{"v":2}""")
        state.add(original)
        state.add(updated)
        assertEquals("Re-adding with the same ID must not duplicate", 1, state.items.size)
        assertEquals("Replaced", state.items.first().title)
    }

    // -----------------------------------------------------------------------
    // update
    // -----------------------------------------------------------------------

    fun testUpdate_ChangesExistingItem() {
        val item = JsonItem(title = "Old Title", json = """{"x":0}""")
        state.add(item)
        val updated = item.copy(title = "New Title", json = """{"x":99}""")
        state.update(updated)
        assertEquals(1, state.items.size)
        assertEquals("New Title", state.items.first().title)
        assertEquals("""{"x":99}""", state.items.first().json)
    }

    fun testUpdate_NonExistentId_LeavesListUnchanged() {
        val existing = JsonItem(title = "Keep Me", json = """{"k":1}""")
        state.add(existing)
        val phantom = JsonItem(title = "Ghost", json = """{}""") // different auto-generated ID
        state.update(phantom)
        assertEquals(1, state.items.size)
        assertEquals("Keep Me", state.items.first().title)
    }

    // -----------------------------------------------------------------------
    // delete
    // -----------------------------------------------------------------------

    fun testDelete_RemovesCorrectItem() {
        val target = JsonItem(title = "Delete Me", json = """{"del":true}""")
        val keep = JsonItem(title = "Keep Me", json = """{"keep":true}""")
        state.add(target)
        state.add(keep)
        state.delete(target.id)
        assertEquals(1, state.items.size)
        assertEquals("Keep Me", state.items.first().title)
    }

    fun testDelete_NonExistentId_LeavesListUnchanged() {
        val item = JsonItem(title = "Safe", json = """{}""")
        state.add(item)
        state.delete("no-such-id")
        assertEquals(1, state.items.size)
    }

    fun testDelete_EmptyList_DoesNotThrow() {
        state.delete("any-id") // must not throw
        assertEquals(0, state.items.size)
    }

    // -----------------------------------------------------------------------
    // contains
    // -----------------------------------------------------------------------

    fun testContains_ReturnsTrueWhenTitleExists() {
        state.add(JsonItem(title = "My Snippet", json = """{}"""))
        assertTrue(state.contains("My Snippet"))
    }

    fun testContains_ReturnsFalseWhenTitleAbsent() {
        state.add(JsonItem(title = "Present", json = """{}"""))
        assertFalse(state.contains("Absent"))
    }

    fun testContains_IsCaseSensitive() {
        state.add(JsonItem(title = "CaseSensitive", json = """{}"""))
        assertFalse(
            "contains() should be case-sensitive",
            state.contains("casesensitive")
        )
    }

    fun testContains_EmptyList_ReturnsFalse() {
        assertFalse(state.contains("anything"))
    }
}
