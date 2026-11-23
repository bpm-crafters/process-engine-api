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
  private var startupFailure: Exception? = null

  @Volatile
  private var phase: Int = ProcessApplicationPhase.NOT_STARTED

  override fun getPhase(): Int = phase

  @Synchronized
  override fun register(participant: ProcessApplicationLifecycleParticipant) {
    rejectIfStartupFailed()
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
    rejectIfStartupFailedUnlessShuttingDown(targetPhase)
    if (phase >= targetPhase) {
      return
    }
    if (startupFailure == null) {
      check(
        phase >= ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION ||
          targetPhase < ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION ||
          registrationContext != null
      ) {
        "Lifecycle transition to phase ${describe(targetPhase)} requires a TaskSubscriptionRegistrationContext"
      }
    }
    check(!transitioning) { "A lifecycle transition is already in progress" }

    transitioning = true
    try {
      val failures = mutableListOf<Exception>()
      namedPhases
        .filter { it > phase && it <= targetPhase && (startupFailure == null || it >= ProcessApplicationPhase.STOPPING) }
        .forEach { namedPhase ->
          failures += transitionToNamedPhase(namedPhase, targetContext, registrationContext)
        }
      if (phase < targetPhase) {
        enter(targetPhase)
        failures += dispatch(targetPhase, targetContext)
        reach(targetPhase)
      }
      throwIfShutdownFailed(failures)
    } finally {
      transitioning = false
    }
  }

  private fun transitionToNamedPhase(
    namedPhase: Int,
    targetContext: ProcessApplicationLifecycleContext,
    registrationContext: TaskSubscriptionRegistrationContext?
  ): List<Exception> {
    val context = if (namedPhase == ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION) {
      checkNotNull(registrationContext) {
        "Lifecycle transition to phase ${describe(targetContext.getPhase())} requires a TaskSubscriptionRegistrationContext"
      }
    } else if (namedPhase == targetContext.getPhase() && startupFailure == null) {
      targetContext
    } else {
      ProcessApplicationLifecycleContext(namedPhase, startupFailure)
    }
    enter(namedPhase)
    val failures = dispatch(namedPhase, context)
    reach(namedPhase)
    return failures
  }

  private fun dispatch(next: Int, context: ProcessApplicationLifecycleContext): List<Exception> {
    val failures = mutableListOf<Exception>()
    participantsForTransition(next).forEach { participant ->
      try {
        participant.onPhase(contextFor(participant.getPhase(), context))
      } catch (failure: Exception) {
        logParticipantFailure(next, participant, failure)
        if (next < ProcessApplicationPhase.STOPPING) {
          startupFailure = failure
          throw failure
        }
        failures += failure
      }
    }
    return failures
  }

  private fun contextFor(
    participantPhase: Int,
    targetContext: ProcessApplicationLifecycleContext
  ): ProcessApplicationLifecycleContext =
    if (participantPhase == targetContext.getPhase() && startupFailure == null) {
      targetContext
    } else {
      ProcessApplicationLifecycleContext(participantPhase, startupFailure)
    }

  private fun participantsForTransition(next: Int): List<ProcessApplicationLifecycleParticipant> =
    participants
      .filter {
        it.getPhase() in (phase + 1)..next &&
          (startupFailure == null || it.getPhase() >= ProcessApplicationPhase.STOPPING)
      }
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

  private fun rejectIfStartupFailed() {
    startupFailure?.let { failure ->
      throw IllegalStateException("Lifecycle is unusable after a participant failed during startup", failure)
    }
  }

  private fun rejectIfStartupFailedUnlessShuttingDown(targetPhase: Int) {
    if (targetPhase < ProcessApplicationPhase.STOPPING) {
      rejectIfStartupFailed()
    }
  }

  private fun throwIfShutdownFailed(failures: List<Exception>) {
    if (failures.isEmpty()) {
      return
    }
    throw IllegalStateException("One or more lifecycle participants failed during shutdown").also { aggregate ->
      failures.forEach(aggregate::addSuppressed)
    }
  }

  private fun logParticipantFailure(
    phase: Int,
    participant: ProcessApplicationLifecycleParticipant,
    failure: Exception
  ) {
    logger.error(failure) {
      "PROCESS-ENGINE-API-LIFECYCLE-003: Lifecycle phase ${describe(phase)} participant " +
        "${participant.javaClass.name} failed."
    }
  }
}
