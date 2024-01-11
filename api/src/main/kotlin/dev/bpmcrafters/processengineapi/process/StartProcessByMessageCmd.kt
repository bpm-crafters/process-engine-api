package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.PayloadSupplier

data class StartProcessByMessageCmd (
  val messageName: String,
  val payload: () -> Map<String, Any>
) : PayloadSupplier {

  override fun payload(): () -> Map<String, Any> = payload
}
