package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.PayloadSupplier

data class StartProcessByDefinitionCmd (
  val definitionKey: String,
  val payload: () -> Map<String, Any>
) : PayloadSupplier {

  override fun payload(): () -> Map<String, Any> = payload
}
