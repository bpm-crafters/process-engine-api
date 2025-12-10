package dev.bpmcrafters.processengineapi.decision

import dev.bpmcrafters.processengineapi.PayloadSupplier
import java.util.function.Supplier

/**
 * Command to evaluate decision by provided reference.
 * @since 1.4
 */
data class DecisionByRefEvaluationCommand(
  /**
   * Decision reference.
   */
  val decisionRef: String,
  /**
   * Payload supplier to pass to decision evaluation.
   */
  val payloadSupplier: PayloadSupplier,
  /**
   * Restrictions supplier to pass to this evaluation.
   */
  val restrictionSupplier: Supplier<Map<String, String>>
) : DecisionEvaluationCommand, PayloadSupplier by payloadSupplier {

  /**
   * Constructs an evaluate command.
   * @param decisionRef decision reference.
   * @param payload payload to use.
   * @param restrictions restrictions for the message.
   */
  constructor(decisionRef: String, payload: Map<String, Any>, restrictions: Map<String, String>) : this(
    decisionRef = decisionRef,
    payloadSupplier = PayloadSupplier { payload },
    restrictionSupplier = { restrictions }
  )

}
