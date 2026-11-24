package dev.bpmcrafters.processengineapi.lifecycle

/**
 * Performs synchronous work at an integer lifecycle phase.
 *
 * Participants are invoked in ascending phase order while the coordinator crosses a named
 * lifecycle boundary. They run while the lifecycle coordinator monitor is held: do not wait for a
 * thread that calls lifecycle registration or transition methods. A startup exception blocks later
 * startup transitions but still permits shutdown; shutdown exceptions are collected after all
 * participants have run.
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
