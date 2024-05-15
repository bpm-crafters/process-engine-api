package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to send a signal to process engine.
 * @since 0.0.1
 */
data class SendSignalCmd(
  val signalName: String,
  val payloadSupplier: PayloadSupplier,
  val restrictions: Map<String, String>
) : PayloadSupplier by payloadSupplier
