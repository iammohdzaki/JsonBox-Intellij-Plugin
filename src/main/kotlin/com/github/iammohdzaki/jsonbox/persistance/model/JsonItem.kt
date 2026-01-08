package com.github.iammohdzaki.jsonbox.persistance.model

import java.util.*

/**
 * Represents a saved JSON snippet.
 *
 * @property id Unique identifier for the item.
 * @property title The display name of the JSON snippet.
 * @property json The actual JSON content.
 * @property updatedAt Timestamp of the last update.
 */
data class JsonItem(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var json: String = "",
    var updatedAt: Long = System.currentTimeMillis()
)