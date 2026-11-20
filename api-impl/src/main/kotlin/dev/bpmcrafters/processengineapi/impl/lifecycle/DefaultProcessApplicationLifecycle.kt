package dev.bpmcrafters.processengineapi.impl.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleContext
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleParticipant
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.lifecycle.TaskSubscriptionRegistrationContext
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Thread-safe, monotonic [ProcessApplicationLifecycle] implementation.
 *
 * @since 1.8.0
 */
class DefaultProcessApplicationLifecycle : ProcessApplicationLifecycle {

  private companion object {
    val namedPhases = listOf(
      ProcessApplicationPhase.INFRASTRUCTURE_READY,
      ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION,
      ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED,
      ProcessApplicationPhase.RUNNING,
      ProcessApplicationPhase.STOPPING,
      ProcessApplicationPhase.STOPPED
    )
  }

  private val participants = mutableListOf<ProcessApplicationLifecycleParticipant>()
  private var transitioning = false

  @Volatile
  private var phase: Int = ProcessApplicationPhase.NOT_STARTED

  override fun getPhase(): Int = phase

  @Synchronized
  override fun register(participant: ProcessApplicationLifecycleParticipant) {
    check(!transitioning && participant.getPhase() > phase) {
      "Lifecycle participants can only be registered for a phase after ${describe(phase)}"
    }
    participants.add(participant)
  }

  /**
   * Dispatch happens while holding the lifecycle monitor. Consequently concurrent callers cannot
   * observe or start a later phase before all participants completed this one.
   */
  @Synchronized
  override fun transitionTo(phase: Int) {
    transitionTo(ProcessApplicationLifecycleContext(phase), null)
  }

  @Synchronized
  override fun transitionTo(phase: Int, registrationContext: TaskSubscriptionRegistrationContext) {
    transitionTo(ProcessApplicationLifecycleContext(phase), registrationContext)
  }

  @Synchronized
  override fun transitionTo(context: ProcessApplicationLifecycleContext) {
    transitionTo(context, context as? TaskSubscriptionRegistrationContext)
  }

  private fun transitionTo(
    targetContext: ProcessApplicationLifecycleContext,
    registrationContext: TaskSubscriptionRegistrationContext?
  ) {
    val targetPhase = targetContext.getPhase()
    if (phase >= targetPhase) {
      return
    }
    check(
      phase >= ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION ||
        targetPhase < ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION ||
        registrationContext != null
    ) {
      "Lifecycle transition to phase ${describe(targetPhase)} requires a TaskSubscriptionRegistrationContext"
    }
    check(!transitioning) { "A lifecycle transition is already in progress" }

    transitioning = true
    try {
      namedPhases
        .filter { it > phase && it <= targetPhase }
        .forEach { namedPhase ->
          transitionToNamedPhase(namedPhase, targetContext, registrationContext)
        }
      if (phase < targetPhase) {
        enter(targetPhase)
        dispatch(targetPhase, targetContext)
        reach(targetPhase)
      }
    } finally {
      transitioning = false
    }
  }

  private fun transitionToNamedPhase(
    namedPhase: Int,
    targetContext: ProcessApplicationLifecycleContext,
    registrationContext: TaskSubscriptionRegistrationContext?
  ) {
    val context = if (namedPhase == ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION) {
      checkNotNull(registrationContext) {
        "Lifecycle transition to phase ${describe(targetContext.getPhase())} requires a TaskSubscriptionRegistrationContext"
      }
    } else if (namedPhase == targetContext.getPhase()) {
      targetContext
    } else {
      ProcessApplicationLifecycleContext(namedPhase)
    }
    enter(namedPhase)
    dispatch(namedPhase, context)
    reach(namedPhase)
  }

  private fun dispatch(next: Int, context: ProcessApplicationLifecycleContext) {
    participantsForTransition(next).forEach { participant ->
      participant.onPhase(contextFor(participant.getPhase(), context))
    }
  }

  private fun contextFor(
    participantPhase: Int,
    targetContext: ProcessApplicationLifecycleContext
  ): ProcessApplicationLifecycleContext =
    if (participantPhase == targetContext.getPhase()) {
      targetContext
    } else {
      ProcessApplicationLifecycleContext(participantPhase)
    }

  private fun participantsForTransition(next: Int): List<ProcessApplicationLifecycleParticipant> =
    participants
      .filter { it.getPhase() in (phase + 1)..next }
      .sortedBy(ProcessApplicationLifecycleParticipant::getPhase)

  private fun describe(phase: Int): String =
    ProcessApplicationPhase.getName(phase)?.let { "$it ($phase)" } ?: "custom phase ($phase)"

  private fun reach(next: Int) {
    phase = next
    logger.info {
      "PROCESS-ENGINE-API-LIFECYCLE-002: Lifecycle phase ${describe(next)} reached."
    }
  }

  private fun enter(next: Int) {
    logger.info {
      "PROCESS-ENGINE-API-LIFECYCLE-001: Entering lifecycle phase ${describe(next)}."
    }
  }
}
