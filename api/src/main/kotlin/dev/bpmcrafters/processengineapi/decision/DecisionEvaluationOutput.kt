package dev.bpmcrafters.processengineapi.decision

/**
 * Decision output.
 *
 * Represents a result of evaluation. Might be handled
 * @since 1.4
 */
interface DecisionEvaluationOutput {

  /**
   * Returns as a single output value converted to a type.
   * This conversion is an attempt to convert the output to the given type and might fail, if the type is incompatible.
   * @param T type of the output.
   */
  fun <T> asType(): T?

  /**
   * Returns as multi-output map, keyed by output name. If names are not available, use digits as fallbacks.
   */
  fun asMap(): Map<String, Any?>

  /**
   *  Additional metadata on evaluation output, if supported by the engine.
   */
  fun meta(): Map<String, String> = mapOf()
}
