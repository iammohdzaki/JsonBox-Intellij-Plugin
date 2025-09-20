package com.github.iammohdzaki.jsonbox.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.ui.Messages

object JsonUtils {

    private val gson: Gson = GsonBuilder().create()

    /** Validate JSON string */
    fun validateJson(json: String?): String? {
        if (json.isNullOrBlank()) return "JSON is empty"
        return try {
            JsonParser.parseString(json)
            null
        } catch (e: JsonSyntaxException) {
            e.message
        } catch (e: Exception) {
            e.message ?: "Unknown error"
        }
    }


    fun stringifyJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            if (isStringified(json)) {
                json
            } else {
                val element = tryParseJson(json)
                Gson().toJson(element.toString()) // wrap as string literal
            }
        } catch (e: Exception) {
            Messages.showErrorDialog("${e.message}", "Stringify Error")
            null
        }
    }

    fun deStringifyJson(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val element = tryParseJson(unescapeJsonString(json))
            gson.toJson(element)
        } catch (e: Exception) {
            Messages.showErrorDialog("${e.message}", "DeStringify Error")
            null
        }
    }

    /** Tries to parse JSON, even if it’s wrapped in quotes */
    private fun tryParseJson(json: String): com.google.gson.JsonElement {
        return try {
            JsonParser.parseString(json)
        } catch (_: Exception) {
            JsonParser.parseString(unescapeJsonString(json))
        }
    }

    /** Unescape stringified JSON */
    private fun unescapeJsonString(json: String): String {
        return json.trim()
            .removeSurrounding("\"")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    /** Checks if a JSON string is already a string literal (escaped JSON) */
    private fun isStringified(json: String): Boolean {
        val trimmed = json.trim()
        return trimmed.startsWith("\"") && trimmed.endsWith("\"")
    }
}