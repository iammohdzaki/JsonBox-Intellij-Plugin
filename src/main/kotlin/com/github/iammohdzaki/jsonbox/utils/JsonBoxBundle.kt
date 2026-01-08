package com.github.iammohdzaki.jsonbox.utils

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.JsonBoxBundle"

/**
 * Access to the plugin's resource bundle for localized strings.
 */
object JsonBoxBundle : DynamicBundle(BUNDLE) {

    /**
     * Retrieves a localized message for the given key and optional parameters.
     *
     * @param key The key of the message in the resource bundle.
     * @param params Optional parameters for the message.
     * @return The localized string.
     */
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): String = getMessage(key, *params)
}