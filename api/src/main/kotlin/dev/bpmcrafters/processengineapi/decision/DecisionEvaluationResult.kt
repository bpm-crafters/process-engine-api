package dev.bpmcrafters.processengineapi.decision

/**
 * Represents result of decision evaluation.
 */
sealed interface DecisionEvaluationResult {
  /**
   * Returns the result as single.
   */
  fun single(): SingleDecisionEvaluationResult {
    require(this is SingleDecisionEvaluationResult) { "Decision evaluation result must be a single but it was ${this::class.simpleName}" }
    return this
  }

  /**
   * Returns the result as collect.
   */
  fun collect(): CollectDecisionEvaluationResult {
    require(this is CollectDecisionEvaluationResult) { "Decision evaluation result must be a collect but it was ${this::class.simpleName}" }
    return this
  }
}
