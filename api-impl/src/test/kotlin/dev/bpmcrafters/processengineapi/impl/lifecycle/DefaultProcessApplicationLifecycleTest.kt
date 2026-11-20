package dev.bpmcrafters.processengineapi.impl.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleContext
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleParticipant
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.lifecycle.TaskSubscriptionRegistrationContext
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
internal class DefaultProcessApplicationLifecycleTest {

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

  private val taskSubscriptionApi = mock<TaskSubscriptionApi>()

  @Test
  fun `dispatches named participants synchronously in phase order`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val phases = mutableListOf<Int>()
    namedPhases.forEach { phase ->
      lifecycle.register(participant(phase) { context ->
        assertThat(context.getPhase()).isEqualTo(phase)
        if (context is TaskSubscriptionRegistrationContext) {
          assertThat(context.getTaskSubscriptionApi()).isSameAs(taskSubscriptionApi)
        }
        phases += phase
      })
    }

    transitionThroughNamedPhases(lifecycle)

    assertThat(phases).containsExactlyElementsOf(namedPhases)
    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.STOPPED)
  }

  @Test
  fun `runs arbitrary phases in order between named boundaries`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<String>()
    lifecycle.register(participant(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED + 11) { calls += "second" })
    lifecycle.register(participant(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED + 10) { calls += "first" })
    lifecycle.register(participant(ProcessApplicationPhase.RUNNING) { calls += "running" })

    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    lifecycle.transitionTo(TaskSubscriptionRegistrationContext(taskSubscriptionApi))
    lifecycle.transitionTo(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED)
    lifecycle.transitionTo(ProcessApplicationPhase.RUNNING)

    assertThat(calls).containsExactly("first", "second", "running")
  }

  @Test
  fun `drives every missing named phase before running`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<Int>()
    namedPhases.forEach { phase ->
      lifecycle.register(participant(phase) { calls += phase })
    }

    lifecycle.transitionTo(
      ProcessApplicationPhase.RUNNING,
      TaskSubscriptionRegistrationContext(taskSubscriptionApi)
    )

    assertThat(calls).containsExactly(
      ProcessApplicationPhase.INFRASTRUCTURE_READY,
      ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION,
      ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED,
      ProcessApplicationPhase.RUNNING
    )
    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.RUNNING)
  }

  @Test
  fun `requires registration context when target crosses subscription registration`() {
    val lifecycle = DefaultProcessApplicationLifecycle()

    assertThatIllegalStateException().isThrownBy {
      lifecycle.transitionTo(ProcessApplicationPhase.RUNNING)
    }.withMessage("Lifecycle transition to phase RUNNING (42) requires a TaskSubscriptionRegistrationContext")

    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.NOT_STARTED)
  }

  @Test
  fun `logs entering and reaching a phase`(output: CapturedOutput) {
    val lifecycle = DefaultProcessApplicationLifecycle()

    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)

    assertThat(output.all).contains(
      "PROCESS-ENGINE-API-LIFECYCLE-001: Entering lifecycle phase " +
        "INFRASTRUCTURE_READY (${ProcessApplicationPhase.INFRASTRUCTURE_READY})."
    )
    assertThat(output.all).contains(
      "PROCESS-ENGINE-API-LIFECYCLE-002: Lifecycle phase " +
        "INFRASTRUCTURE_READY (${ProcessApplicationPhase.INFRASTRUCTURE_READY}) reached."
    )
  }

  @Test
  fun `allows participants to be registered for a future phase`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<Int>()

    lifecycle.transitionTo(
      ProcessApplicationPhase.RUNNING,
      TaskSubscriptionRegistrationContext(taskSubscriptionApi)
    )
    lifecycle.register(participant(ProcessApplicationPhase.STOPPING) {
      calls += ProcessApplicationPhase.STOPPING
    })
    lifecycle.transitionTo(ProcessApplicationPhase.STOPPING)

    assertThat(calls).containsExactly(ProcessApplicationPhase.STOPPING)
  }

  @Test
  fun `skips phases that have already been reached`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<Int>()
    lifecycle.register(participant(ProcessApplicationPhase.INFRASTRUCTURE_READY) {
      calls += ProcessApplicationPhase.INFRASTRUCTURE_READY
    })

    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    lifecycle.transitionTo(ProcessApplicationPhase.NOT_STARTED)

    assertThat(calls).containsExactly(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
  }

  @Test
  fun `does not move lifecycle backwards`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<Int>()
    lifecycle.register(participant(ProcessApplicationPhase.RUNNING) {
      calls += ProcessApplicationPhase.RUNNING
    })
    lifecycle.register(participant(ProcessApplicationPhase.STOPPING) {
      calls += ProcessApplicationPhase.STOPPING
    })

    lifecycle.transitionTo(
      ProcessApplicationPhase.RUNNING,
      TaskSubscriptionRegistrationContext(taskSubscriptionApi)
    )
    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    lifecycle.transitionTo(ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION)

    assertThat(calls).containsExactly(ProcessApplicationPhase.RUNNING)
    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.RUNNING)
  }

  @Test
  fun `allows a manual driver to advance lifecycle before another driver`() {
    val lifecycle = DefaultProcessApplicationLifecycle()

    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    lifecycle.transitionTo(TaskSubscriptionRegistrationContext(taskSubscriptionApi))
    lifecycle.transitionTo(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED)
    lifecycle.transitionTo(ProcessApplicationPhase.RUNNING)

    transitionThroughNamedPhases(lifecycle)

    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.STOPPED)
  }

  @Test
  fun `keeps phase unchanged when a participant fails`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    lifecycle.register(participant(ProcessApplicationPhase.INFRASTRUCTURE_READY) {
      throw IllegalStateException("registration failed")
    })

    assertThatIllegalStateException().isThrownBy {
      lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    }.withMessage("registration failed")

    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.NOT_STARTED)
  }

  private fun participant(
    phase: Int,
    action: (ProcessApplicationLifecycleContext) -> Unit
  ): ProcessApplicationLifecycleParticipant = object : ProcessApplicationLifecycleParticipant {
    override fun getPhase(): Int = phase

    override fun onPhase(context: ProcessApplicationLifecycleContext) {
      action(context)
    }
  }

  private fun transitionThroughNamedPhases(lifecycle: DefaultProcessApplicationLifecycle) {
    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    lifecycle.transitionTo(TaskSubscriptionRegistrationContext(taskSubscriptionApi))
    lifecycle.transitionTo(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED)
    lifecycle.transitionTo(ProcessApplicationPhase.RUNNING)
    lifecycle.transitionTo(ProcessApplicationPhase.STOPPING)
    lifecycle.transitionTo(ProcessApplicationPhase.STOPPED)
  }
}
