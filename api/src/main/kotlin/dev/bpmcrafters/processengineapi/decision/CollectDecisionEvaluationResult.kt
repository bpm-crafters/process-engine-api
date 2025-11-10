package dev.bpmcrafters.processengineapi.decision

/**
 * Decision evaluation result for all collect-valued hit policies.
 * @since 2.0
 */
data class CollectDecisionEvaluationResult(
  val result: List<DecisionEvaluationOutput>
) : DecisionEvaluationResult
