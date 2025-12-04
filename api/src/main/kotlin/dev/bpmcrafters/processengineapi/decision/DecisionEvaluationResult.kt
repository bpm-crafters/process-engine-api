package dev.bpmcrafters.processengineapi.decision

/**
 * Represents the result of decision evaluation.
 * @since 1.4
 */
interface DecisionEvaluationResult {
  /**
   * Returns the result excepted to be a single value.
   *
   * This is because the hit policy defined it to be single (single result or result of aggregation).
   */
  fun asSingle(): DecisionEvaluationOutput?

  /**
   * Returns the result expected to be a collection of values.
   *
   * This is because multiple rules have fired, and we collect multiple results without aggregation.
   */
  fun asList(): List<DecisionEvaluationOutput>

  /**
   *  Additional metadata on evaluation result, if supported by the engine.
   */
  fun meta(): Map<String, String>
}
