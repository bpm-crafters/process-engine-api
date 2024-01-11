package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.PayloadSupplier

data class CorrelateMessageCmd(
  val messageName: String,
  val payload: () -> Map<String, Any>,
  val correlation: () -> Correlation
) : PayloadSupplier {
  override fun payload(): () -> Map<String, Any> = payload
}
