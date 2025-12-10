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
   * @param type class which the output will be cast to
   */
  fun <T: Any> asType(type: Class<T>): T?

  /**
   * Returns as multi-output map, keyed by output name. Attempt on converting single output value into Map would result in a runtime exception
   */
  fun asMap(): Map<String, Any?>?

  /**
   *  Additional metadata on evaluation output, if supported by the engine.
   */
  fun meta(): Map<String, String> = mapOf()
}
