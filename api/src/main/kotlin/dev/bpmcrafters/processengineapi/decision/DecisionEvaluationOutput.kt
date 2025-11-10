package dev.bpmcrafters.processengineapi.decision

/**
 * Decision output.
 * @since 2.0
 */
data class DecisionEvaluationOutput(
  /**
   * Keys are output names pointing to values.
   */
  val values: Map<String, Any>,
  /**
   * Additional metadata about the task.
   */
  val meta: Map<String, String> = emptyMap()
)
