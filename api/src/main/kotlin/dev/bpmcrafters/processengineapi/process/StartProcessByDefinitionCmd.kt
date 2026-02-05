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
  val payloadSupplier: PayloadSupplier,
  /**
   * Restrictions applied for this message.
   */
  val restrictions: Map<String, String> = emptyMap()
) : StartProcessCommand, PayloadSupplier by payloadSupplier {
  /**
   * Constructs a start command with definition key and payload.
   * @param definitionKey process definition key.
   * @param payload payload to use.
   * @param restrictions restrictions for the message.
   */
  constructor(definitionKey: String, payload: Map<String, Any?>, restrictions: Map<String, String>) : this(
    definitionKey = definitionKey,
    payloadSupplier = PayloadSupplier { payload },
    restrictions = restrictions
  )
  /**
   * Constructs a start command with definition key and payload.
   * @param definitionKey process definition key.
   * @param payload payload to use.
   */
  constructor(definitionKey: String, payload: Map<String, Any?>) : this(
    definitionKey = definitionKey,
    payloadSupplier = PayloadSupplier { payload } ,
    restrictions = mapOf()
  )

  /**
   * Constructs a start command with definition key and no payload.
   * @param definitionKey process definition key.
   */
  constructor(definitionKey: String) : this(definitionKey, mapOf() )
}
