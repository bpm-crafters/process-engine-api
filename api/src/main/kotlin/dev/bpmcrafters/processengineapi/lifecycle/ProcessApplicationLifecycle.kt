package dev.bpmcrafters.processengineapi.lifecycle

/**
 * Coordinates the portable process-application lifecycle.
 *
 * Participants must be registered before their target phase is reached; registration for a future
 * phase is allowed. A startup failure blocks further startup transitions but permits shutdown
 * transitions; shutdown participants receive that failure in their context. During shutdown, every
 * participant is invoked and failures are reported after the phase has been reached. A requested
 * phase that has already been reached is skipped.
 *
 * @since 1.8.0
 */
interface ProcessApplicationLifecycle {

  /**
   * Returns the current phase.
   */
  fun getPhase(): Int

  /**
   * Registers a participant that will receive its phase transition when that phase is still ahead
   * of the current lifecycle phase.
   */
  fun register(participant: ProcessApplicationLifecycleParticipant)

  /**
   * Transitions the lifecycle to [phase] and all missing named phases before it, dispatching
   * participants synchronously.
   *
   * If subscription registration has not already been reached, use the overload accepting a
   * [TaskSubscriptionRegistrationContext].
   */
  fun transitionTo(phase: Int)

  /**
   * Transitions the lifecycle to [phase] and all missing named phases before it, supplying
   * [registrationContext] when subscription registration is reached.
   */
  fun transitionTo(phase: Int, registrationContext: TaskSubscriptionRegistrationContext)

  /**
   * Transitions the lifecycle to the phase represented by [context] and all missing named phases
   * before it.
   */
  fun transitionTo(context: ProcessApplicationLifecycleContext)
}
