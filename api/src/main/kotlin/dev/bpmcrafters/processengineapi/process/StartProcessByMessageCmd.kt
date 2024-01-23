package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Starts a new process instance by message.
 * @since 0.0.1
 */
data class StartProcessByMessageCmd(
  /**
   * Name of the message.
   */
  val messageName: String,
  /**
   * Payload supplier.
   */
  val payloadSupplier: PayloadSupplier
) : StartProcessCommand, PayloadSupplier by payloadSupplier
