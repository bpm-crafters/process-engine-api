package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to start a new process instance by process definition key.
 * @since 0.0.1
 */
data class StartProcessByDefinitionCmd(
  /**
   * Process definition key.
   */
  val definitionKey: String,
  /**
   * Payload supplier to pass to the new process instance.
   */
  val payloadSupplier: PayloadSupplier
) : StartProcessCommand, PayloadSupplier by payloadSupplier
