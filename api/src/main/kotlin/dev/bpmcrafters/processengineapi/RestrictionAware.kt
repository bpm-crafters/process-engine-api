package dev.bpmcrafters.processengineapi

/**
 * Interface to retrieve information of supported restrictions.
 * @since 0.0.1
 */
interface RestrictionAware {
  /**
   * Retrieves a set of restriction keys supported by implementer.
   * @return set of supported restrictions.
   */
  fun getSupportedRestrictions(): Set<String>

  /**
   * Check that the supplied restrictions are supported by the implementer.
   * @return true if all restriction keys are supported, false otherwise.
   */
  fun areSupported(restrictions: Map<String, String>): Boolean = getSupportedRestrictions().let { supported ->
    restrictions.keys.all { supported.contains(it) }
  }

  /**
   * Checks that the supplied restrictions are supported by the implementer
   * @return restrictions if all restrictions are supported
   * @throws IllegalArgumentException if not all restrictions are supported
   */
  fun ensureSupported(restrictions: Map<String, String>): Map<String, String> {
    require(areSupported(restrictions)) { "Only ${getSupportedRestrictions().joinToString(", ")} are supported but ${restrictions.keys.joinToString(", ")} were found." }
    return restrictions
  }
}
