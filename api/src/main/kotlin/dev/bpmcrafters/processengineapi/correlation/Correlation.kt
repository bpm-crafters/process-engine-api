package dev.bpmcrafters.processengineapi.correlation

/**
 * Correlation information.
 * @since 0.0.1
 */
data class Correlation(
  /**
   * Restrictions to find unique process instance for correlation.
   */
  val restrictions: Map<String, String>
)
