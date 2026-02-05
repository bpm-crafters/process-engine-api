package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to start a new process instance at a specific element.
 * @since 1.5.0
 */
data class StartProcessByDefinitionAtElementCmd(
  /**
   * Process definition key.
   */
  val definitionKey: String,
  /**
   * ID of the element to start the process at.
   */
  val elementId: String,
  /**
   * Payload supplier to pass to the new process instance.
   */
  val payloadSupplier: PayloadSupplier,
  /**
   * Restrictions applied for this start command.
   */
  val restrictions: Map<String, String> = emptyMap()
) : StartProcessCommand, PayloadSupplier by payloadSupplier {
  /**
   * Constructs a start command with a definition key, element ID, payload, and restrictions.
   * @param definitionKey process definition key.
   * @param elementId element ID to start the process at.
   * @param payload payload to use.
   * @param restrictions restrictions for the start command.
   */
  constructor(definitionKey: String, elementId: String, payload: Map<String, Any?>, restrictions: Map<String, String>) : this(
    definitionKey = definitionKey,
    elementId = elementId,
    payloadSupplier = PayloadSupplier { payload },
    restrictions = restrictions
  )
  /**
   * Constructs a start command with a definition key, element ID, and payload.
   * @param definitionKey process definition key.
   * @param elementId element ID to start the process at.
   * @param payload payload to use.
   */
  constructor(definitionKey: String, elementId: String, payload: Map<String, Any?>) : this(
    definitionKey = definitionKey,
    elementId = elementId,
    payloadSupplier = PayloadSupplier { payload } ,
    restrictions = mapOf()
  )

  /**
   * Constructs a start command with definition key, element ID, and no payload.
   * @param definitionKey process definition key.
   * @param elementId element ID to start the process at.
   */
  constructor(definitionKey: String, elementId: String) : this(
    definitionKey = definitionKey,
    elementId = elementId,
    payload = mapOf(),
  )
}
