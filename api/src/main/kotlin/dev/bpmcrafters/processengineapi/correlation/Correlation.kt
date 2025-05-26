package dev.bpmcrafters.processengineapi.correlation

/**
 * Correlation information.
 * @since 0.0.1
 */
data class Correlation private constructor(
  /**
   * Correlation key. The value provided during correlation needs to match the value hold by the instance.
   */
  val correlationKey: String,
  /**
   * If the engine is not supporting the correlation by key, defines the name of the local (to the receive task or event) variable holding the value of the key.
   */
  val correlationVariable: String = "correlationKey"
) {
  companion object {
    /**
     * Constructs correlation based on key, using default variable name.
     * @param key correlation key.
     * @return correlation
     */
    @JvmStatic
    fun withKey(key: String): Correlation {
      return Correlation(correlationKey = key)
    }
  }

  /**
   * Modifies the name for correlation variable.
   * @param variable name of the (local) variable used to correlate.
   * @return correlation.
   */
  fun withVariable(variable: String): Correlation {
    return this.copy(correlationVariable = variable)
  }
}
