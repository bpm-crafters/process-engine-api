package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.PayloadSupplier

/**
 * Command to start a new process instance by message at a specific element.
 * Useful for processes that have no plain (none) start event and are entered
 * only via one or multiple message start events.
 * @since 1.7
 */
data class StartProcessByMessageAtElementCmd(
  /**
   * Name of the message.
   */
  val messageName: String,
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
   * Constructs a start command with a message name, element ID, payload, and restrictions.
   * @param messageName message name.
   * @param elementId element ID to start the process at.
   * @param payload payload to use.
   * @param restrictions restrictions for the start command.
   */
  constructor(messageName: String, elementId: String, payload: Map<String, Any?>, restrictions: Map<String, String>) : this(
    messageName = messageName,
    elementId = elementId,
    payloadSupplier = PayloadSupplier { payload },
    restrictions = restrictions
  )
  /**
   * Constructs a start command with a message name, element ID, and payload.
   * @param messageName message name.
   * @param elementId element ID to start the process at.
   * @param payload payload to use.
   */
  constructor(messageName: String, elementId: String, payload: Map<String, Any?>) : this(
    messageName = messageName,
    elementId = elementId,
    payloadSupplier = PayloadSupplier { payload } ,
    restrictions = mapOf()
  )

  /**
   * Constructs a start command with message name, element ID, and no payload.
   * @param messageName message name.
   * @param elementId element ID to start the process at.
   */
  constructor(messageName: String, elementId: String) : this(
    messageName = messageName,
    elementId = elementId,
    payload = mapOf(),
  )
}
