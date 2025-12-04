package dev.bpmcrafters.processengineapi.decision

/**
 * Decision output.
 * @since 1.4
 */

interface DecisionEvaluationOutput {
  /**
   * Returns as a single output value
   */
  fun <T> withSingleOutput(): T?
  /**
   * Returns as multi-outputs with names
   */
  fun withMultipleOutputs(): Map<String, Any?>?
}
