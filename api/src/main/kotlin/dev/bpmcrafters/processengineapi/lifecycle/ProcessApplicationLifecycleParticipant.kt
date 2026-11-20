package dev.bpmcrafters.processengineapi.lifecycle

/**
 * Performs synchronous work at an integer lifecycle phase.
 *
 * Participants are invoked in ascending phase order while the coordinator crosses a named
 * lifecycle boundary. An exception aborts the transition and is propagated to the lifecycle driver.
 *
 * @since 1.8.0
 */
interface ProcessApplicationLifecycleParticipant {

  /**
   * Returns the integer lifecycle phase at which this participant runs.
   */
  fun getPhase(): Int

  /**
   * Performs this participant's lifecycle work.
   */
  fun onPhase(context: ProcessApplicationLifecycleContext)
}
