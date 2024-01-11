package dev.bpmcrafters.processengineapi.process

data class StartProcessResponse(
  val instanceId: String,
  val meta: Map<String, String>
)
