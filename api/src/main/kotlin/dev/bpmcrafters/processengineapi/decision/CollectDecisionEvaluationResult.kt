package dev.bpmcrafters.processengineapi.decision

/**
 * Decision evaluation result for all collect-valued hit policies.
 * @since 1.4
 */
data class CollectDecisionEvaluationResult(
  val result: List<DecisionEvaluationOutput>,
  override val meta: Map<String, String> = emptyMap()
) : DecisionEvaluationResult
