package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to correlate a message with a running process instance.
 * @since 0.0.1
 */
data class CorrelateMessageCmd(
  /**
   * The message name as defined in the BPMN process.
   */
  val messageName: String,
  /**
   * Payload supplier.
   */
  val payloadSupplier: PayloadSupplier,
  /**
   * Correlation supplier.
   */
  val correlation: CorrelationSupplier,
  /**
   * Restrictions applied for this message.
   */
  val restrictions: Map<String, String> = emptyMap()
) : PayloadSupplier by payloadSupplier {
  /**
   * Constructs a correlation command by message name, payload and correlation.
   * @param messageName message name.
   * @param payload payload to use.
   * @param correlation correlation to use.
   */
  constructor(messageName: String, payload: Map<String, Any>, correlation: Correlation, restrictions: Map<String, String>) :
    this(
      messageName = messageName,
      payloadSupplier = PayloadSupplier { payload },
      correlation = CorrelationSupplier { correlation},
      restrictions = restrictions
    )

  /**
   * Constructs a correlation command by message name, payload and correlation.
   * @param messageName message name.
   * @param payload payload to use.
   * @param correlation correlation to use.
   */
  constructor(messageName: String, payload: Map<String, Any>, correlation: Correlation) :
    this(
      messageName = messageName,
      payloadSupplier = PayloadSupplier { payload },
      correlation = CorrelationSupplier { correlation},
      restrictions = mapOf()
    )
  /**
   * Constructs a correlation command by message name, no payload and correlation.
   * @param messageName message name.
   * @param correlation correlation to use.
   */
  constructor(messageName: String, correlation: Correlation) : this(messageName, mapOf() , correlation)

}
