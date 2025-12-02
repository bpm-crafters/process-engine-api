package dev.bpmcrafters.processengineapi.decision

/**
 * Decision evaluation output representing a single anonymous output
 *
 *  @since 1.4
 */
data class DecisionEvaluationSingleOutput(
  val output: Any?
): DecisionEvaluationOutput
