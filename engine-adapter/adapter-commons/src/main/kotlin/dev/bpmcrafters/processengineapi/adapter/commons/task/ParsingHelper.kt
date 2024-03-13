package dev.bpmcrafters.processengineapi.adapter.commons.task

interface ParsingHelper {

  fun parseJsonString(value: String): Map<String, Any>

  fun parseString(value: String): Map<String, Any> {
    return value.split(",").map { it.trim() }
      .associate { keyWithValue -> keyWithValue.split("=").let { keyAndValue -> keyAndValue[0] to keyAndValue[1] } }
  }
}
