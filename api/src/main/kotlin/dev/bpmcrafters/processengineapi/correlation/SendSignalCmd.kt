package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to send a signal to process engine.
 * @since 0.0.1
 */
data class SendSignalCmd(
  /**
   * Signal name.
   */
  val signalName: String,
  /**
   * Payload supplier.
   */
  val payloadSupplier: PayloadSupplier,
  /**
   * Restrictions of the signaling.
   */
  val restrictions: Map<String, String> = emptyMap()
) : PayloadSupplier by payloadSupplier {
  /**
   * Constructs a signal command by signal name, restrictions and given payload.
   * @param signalName signal name.
   * @param restrictions restrictions.
   * @param payload payload to use.
   */
  constructor(signalName: String, restrictions: Map<String, String>, payload: Map<String, Any>) : this(signalName, PayloadSupplier { payload }, restrictions )
  /**
   * Constructs a signal command by signal name, restrictions and no payload.
   * @param signalName signal name.
   * @param restrictions restrictions.
   */
  constructor(signalName: String, restrictions: Map<String, String>) : this(signalName, restrictions, mapOf() )

}
