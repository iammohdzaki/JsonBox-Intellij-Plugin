package com.github.iammohdzaki.jsonbox.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.diagnostic.Logger

/**
 * Utility class for JSON-related operations such as validation, stringification, and de-stringification.
 */
object JsonUtils {

    private val gson: Gson = GsonBuilder().create()
    private val logger = Logger.getInstance(JsonUtils::class.java)

    /**
     * Validates a JSON string.
     *
     * @param json The JSON string to validate.
     * @return null if the JSON is valid, otherwise returns the error message.
     */
    fun validateJson(json: String?): String? {
        if (json.isNullOrBlank()) return JsonBoxBundle.message("jsonbox.error.empty")
        return try {
            JsonParser.parseString(json)
            null
        } catch (e: JsonSyntaxException) {
            logger.debug("Failed to validate JSON", e)
            e.message
        } catch (e: Exception) {
            logger.debug("Failed to validate JSON", e)
            e.message ?: JsonBoxBundle.message("jsonbox.error.unknown")
        }
    }

    /**
     * Formats (pretty-prints) a JSON string.
     * This is much faster than PSI-based formatting for large JSON payloads.
     *
     * @param json The JSON string to format.
     * @return The formatted JSON string, or null if it's invalid.
     */
    fun formatJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val jsonElement = JsonParser.parseString(json)
            val prettyGson = GsonBuilder().setPrettyPrinting().create()
            prettyGson.toJson(jsonElement)
        } catch (e: Exception) {
            logger.debug("Failed to format JSON", e)
            null
        }
    }

    /**
     * Minifies a JSON string, removing all unnecessary whitespace and newlines.
     * 
     * @param json The JSON string to minify.
     * @return The minified JSON string, or null if it's invalid.
     */
    fun minifyJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val jsonElement = JsonParser.parseString(json)
            gson.toJson(jsonElement) // Default gson behavior is minified
        } catch (e: Exception) {
            logger.debug("Failed to Minify JSON", e)
            null
        }
    }

    /**
     * Converts a JSON string into a stringified version (escaped and wrapped in quotes).
     *
     * @param json The JSON string to stringify.
     * @return The stringified JSON, or throws an exception if an error occurs.
     */
    fun stringifyJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            if (isStringified(json)) {
                json
            } else {
                gson.toJson(json)
            }
        } catch (e: Exception) {
            logger.warn("Failed to stringify JSON", e)
            throw e
        }
    }

    /**
     * Converts a stringified JSON back to its original form.
     *
     * @param json The stringified JSON.
     * @return The original JSON content, or throws an exception if an error occurs.
     */
    fun deStringifyJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            gson.fromJson(json, String::class.java)
        } catch (e: Exception) {
            logger.warn("Failed to destringify JSON", e)
            throw e
        }
    }

    /**
     * Checks if a JSON string is already a string literal (escaped JSON).
     *
     * @param json The JSON string to check.
     * @return True if it starts and ends with double quotes.
     */
    private fun isStringified(json: String): Boolean {
        val t = json.trim()
        return t.startsWith("\"") && t.endsWith("\"")
    }
}