package com.github.iammohdzaki.jsonbox.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

object JsonUtils {

    private val objectMapper = ObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    fun formatJson(json: String): String? = try {
        val obj = objectMapper.readTree(json)
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    } catch (e: Exception) {
        null
    }

    fun validateJson(json: String): Boolean = try {
        objectMapper.readTree(json)
        true
    } catch (e: Exception) {
        false
    }

    fun toSingleLineJson(json: String): String? = try {
        val obj = objectMapper.readTree(json)
        objectMapper.writeValueAsString(obj.toString())
    } catch (e: Exception) {
        null
    }
}