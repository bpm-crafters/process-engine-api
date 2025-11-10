package dev.bpmcrafters.processengineapi.decision

/**
 * Decision evaluation result for all single-valued hit policies.
 * @since 2.0
 */
data class SingleDecisionEvaluationResult(
  val result: DecisionEvaluationOutput
) : DecisionEvaluationResult
