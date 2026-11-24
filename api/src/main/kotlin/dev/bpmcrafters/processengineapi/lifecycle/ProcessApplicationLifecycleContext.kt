package dev.bpmcrafters.processengineapi.lifecycle

/**
 * Context synchronously supplied to lifecycle participants for a phase.
 *
 * @since 1.8.0
 */
open class ProcessApplicationLifecycleContext @JvmOverloads constructor(
  private val phase: Int,
  private val startupFailure: Exception? = null
) {

  /**
   * Returns the lifecycle phase being dispatched.
   */
  fun getPhase(): Int = phase

  /**
   * Returns the failure that caused this shutdown transition, or `null` during normal operation.
   */
  fun getStartupFailure(): Exception? = startupFailure
}
