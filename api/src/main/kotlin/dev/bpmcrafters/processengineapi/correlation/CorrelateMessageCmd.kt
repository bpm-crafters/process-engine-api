package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to correlate a message with running process instance.
 * @since 0.0.1
 */
data class CorrelateMessageCmd(
  val messageName: String,
  val payloadSupplier: PayloadSupplier,
  val correlation: CorrelationSupplier
) : PayloadSupplier by payloadSupplier
