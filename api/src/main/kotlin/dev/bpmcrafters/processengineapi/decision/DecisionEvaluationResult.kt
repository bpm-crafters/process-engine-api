package dev.bpmcrafters.processengineapi.decision

/**
 * Represents the result of decision evaluation.
 *
 */
interface DecisionEvaluationResult{
  /**
   * Returns the result excepted to be a single value
   */
  fun asSingleValue(): DecisionEvaluationOutput

  /**
   * Returns the result expected to be s collection of values
   */
  fun asCollectValues(): List<DecisionEvaluationOutput>

  /**
  *  Additional metadata on evaluation result.
  */
  fun meta(): Map<String, String>
}
