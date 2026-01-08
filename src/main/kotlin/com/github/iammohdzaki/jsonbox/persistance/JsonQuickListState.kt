package com.github.iammohdzaki.jsonbox.persistance

import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent state component for storing the list of JSON items saved by the user.
 * This state is persisted at the project level in `jsonbox_quick_list.xml`.
 */
@Service(Service.Level.PROJECT)
@State(
    name = "JsonBoxQuickList",
    storages = [Storage("jsonbox_quick_list.xml")]
)
class JsonQuickListState : PersistentStateComponent<JsonQuickListState> {

    /** The list of saved JSON items. */
    var items: MutableList<JsonItem> = mutableListOf()

    /**
     * Adds a new JSON item or replaces an existing one with the same ID.
     *
     * @param item The JSON item to add.
     */
    fun add(item: JsonItem) {
        items.removeIf { it.id == item.id }
        items.add(item)
    }

    /**
     * Updates an existing JSON item in the list.
     *
     * @param item The JSON item with updated content.
     */
    fun update(item: JsonItem) {
        items.replaceAll { if (it.id == item.id) item else it }
    }

    /**
     * Deletes a JSON item by its ID.
     *
     * @param itemId The unique ID of the item to delete.
     */
    fun delete(itemId: String) {
        items.removeIf { it.id == itemId }
    }

    /**
     * Checks if an item with the given title exists in the list.
     *
     * @param itemName The title to search for.
     * @return True if an item with the given title is found.
     */
    fun contains(itemName: String): Boolean {
        return items.find { it.title == itemName } != null
    }

    override fun getState() = this
    override fun loadState(state: JsonQuickListState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}