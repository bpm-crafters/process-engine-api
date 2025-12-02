package dev.bpmcrafters.processengineapi.decision

/**
 * Decision evaluation output representing multiple named values
 * produced by a decision with multiple outputs.
 *
 * @since 1.4
 */
data class DecisionEvaluationMultiOutput (
  val outputs: Map<String, Any?>
) :DecisionEvaluationOutput
