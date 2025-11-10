package dev.bpmcrafters.processengineapi.decision

import dev.bpmcrafters.processengineapi.PayloadSupplier
import java.util.function.Supplier

/**
 * Command to evaluate decision by provided reference.
 * @since 2.0
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
) : DecisionEvaluationCommand, PayloadSupplier by payloadSupplier
