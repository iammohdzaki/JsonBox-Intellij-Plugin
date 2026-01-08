package com.github.iammohdzaki.jsonbox.utils

import java.util.*

/**
 * General utility functions for the JsonBox plugin.
 */
object Utils {
    /**
     * Generates a default name for a new JSON snippet.
     * The name follows the pattern "json-xxxxxx" where "xxxxxx" is a random 6-character suffix.
     *
     * @return A randomly generated default name.
     */
    fun generateDefaultName(): String {
        val suffix = UUID.randomUUID().toString().substring(0, 6)
        return "json-$suffix"
    }
}