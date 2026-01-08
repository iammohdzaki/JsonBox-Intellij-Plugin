package com.github.iammohdzaki.jsonbox.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.ui.Messages

/**
 * Utility class for JSON-related operations such as validation, stringification, and de-stringification.
 */
object JsonUtils {

    private val gson: Gson = GsonBuilder().create()

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
            e.message
        } catch (e: Exception) {
            e.message ?: JsonBoxBundle.message("jsonbox.error.unknown")
        }
    }


    /**
     * Converts a JSON string into a stringified version (escaped and wrapped in quotes).
     *
     * @param json The JSON string to stringify.
     * @return The stringified JSON, or null if an error occurs.
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
            showError(e, "jsonbox.dialog.stringify")
            null
        }
    }

    /**
     * Converts a stringified JSON back to its original form.
     *
     * @param json The stringified JSON.
     * @return The original JSON content, or null if an error occurs.
     */
    fun deStringifyJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            gson.fromJson(json, String::class.java)
        } catch (e: Exception) {
            showError(e, "jsonbox.dialog.deStringify")
            null
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

    /**
     * Shows an error dialog with the message from the exception.
     *
     * @param e The exception that occurred.
     * @param key The resource bundle key for the dialog title.
     */
    private fun showError(e: Exception, key: String) {
        Messages.showErrorDialog(
            e.message ?: JsonBoxBundle.message("jsonbox.error.unknown"),
            JsonBoxBundle.message(key)
        )
    }
}