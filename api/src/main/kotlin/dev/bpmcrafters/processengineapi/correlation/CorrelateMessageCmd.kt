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
) : PayloadSupplier by payloadSupplier {
  /**
   * Constructs a correlation command by message name, payload and correlation.
   * @param messageName message name.
   * @param payload payload to use.
   * @param correlation correlation to use.
   */
  constructor(messageName: String, payload: Map<String, Any>, correlation: Correlation) :
    this(messageName = messageName, payloadSupplier = PayloadSupplier { payload }, correlation = CorrelationSupplier { correlation} )
  /**
   * Constructs a correlation command by message name, no payload and correlation.
   * @param messageName message name.
   * @param correlation correlation to use.
   */
  constructor(messageName: String, correlation: Correlation) : this(messageName, mapOf() , correlation)

}
