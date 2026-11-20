package dev.bpmcrafters.processengineapi.lifecycle

/**
 * Context synchronously supplied to lifecycle participants for a phase.
 *
 * @since 1.8.0
 */
open class ProcessApplicationLifecycleContext(
  private val phase: Int
) {

  /**
   * Returns the lifecycle phase being dispatched.
   */
  fun getPhase(): Int = phase
}
