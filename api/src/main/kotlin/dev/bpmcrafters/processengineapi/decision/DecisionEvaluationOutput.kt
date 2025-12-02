package dev.bpmcrafters.processengineapi.decision

/**
 * Decision output.
 * @since 1.4
 */

sealed interface DecisionEvaluationOutput {
  /**
   * Returns as a single output value
   */
  fun single(): DecisionEvaluationSingleOutput {
    require(this is DecisionEvaluationSingleOutput) { "Decision evaluation single output expected but it was ${this::class.simpleName}" }
    return this
  }

  /**
   * Returns as multi-outputs with names
   */
  fun many(): DecisionEvaluationMultiOutput {
    require(this is DecisionEvaluationMultiOutput) { "Decision evaluation multi output expected but it was ${this::class.simpleName}" }
    return this
  }
}
