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
) : StartProcessCommand, PayloadSupplier by payloadSupplier {
  /**
   * Constructs a start command by message name and payload.
   * @param messageName message name.
   * @param payload payload to use.
   */
  constructor(messageName: String, payload: Map<String, Any>) : this(messageName, PayloadSupplier { payload } )
  /**
   * Constructs a start command by message and no payload.
   * @param messageName message name.
   */
  constructor(messageName: String) : this(messageName, mapOf() )

}
