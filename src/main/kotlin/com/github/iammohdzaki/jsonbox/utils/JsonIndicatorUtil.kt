package com.github.iammohdzaki.jsonbox.utils

import com.github.iammohdzaki.jsonbox.utils.JsonIndicatorUtil.LARGE_JSON_THRESHOLD


/**
 * Utility for JSON validation, size formatting, and determining if a JSON string is "large".
 */
object JsonIndicatorUtil {

    /**
     * Threshold for considering a JSON string as "large", in characters.
     */
    const val LARGE_JSON_THRESHOLD = 500_000 // ~500 KB

    /**
     * Formats the size of the JSON string into a human-readable format (B, KB, or MB).
     *
     * @param json The JSON string to measure.
     * @return A formatted size string.
     */
    fun formatSize(json: String): String {
        val bytes = json.toByteArray(Charsets.UTF_8).size
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Checks if the given JSON string exceeds the [LARGE_JSON_THRESHOLD].
     *
     * @param json The JSON string to check.
     * @return True if the JSON is considered large.
     */
    fun isLarge(json: String): Boolean =
        json.length > LARGE_JSON_THRESHOLD

    /**
     * Validates the JSON string and returns a [ValidationResult].
     *
     * @param json The JSON string to validate.
     * @return The result of the validation.
     */
    fun validate(json: String): ValidationResult {
        if (json.isBlank()) return ValidationResult.Empty

        val error = JsonUtils.validateJson(json)
        return if (error == null)
            ValidationResult.Valid
        else
            ValidationResult.Invalid(error)
    }
}

/**
 * Represents the result of a JSON validation check.
 */
sealed class ValidationResult {
    /** Indicates the JSON is valid. */
    object Valid : ValidationResult()

    /** Indicates the JSON is empty or blank. */
    object Empty : ValidationResult()

    /**
     * Indicates the JSON is invalid.
     * @property error The validation error message.
     */
    data class Invalid(val error: String) : ValidationResult()
}