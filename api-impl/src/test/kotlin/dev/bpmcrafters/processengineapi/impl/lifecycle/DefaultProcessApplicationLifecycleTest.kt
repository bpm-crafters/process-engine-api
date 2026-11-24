package dev.bpmcrafters.processengineapi.impl.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleContext
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleParticipant
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.lifecycle.TaskSubscriptionRegistrationContext
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

@ExtendWith(StandardOutputCaptureExtension::class)
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
  fun `dispatches a custom target phase with its supplied context`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val customPhase = ProcessApplicationPhase.INFRASTRUCTURE_READY + 10
    val context = ProcessApplicationLifecycleContext(customPhase)
    var receivedContext: ProcessApplicationLifecycleContext? = null
    lifecycle.register(participant(customPhase) { receivedContext = it })

    lifecycle.transitionTo(context)

    assertThat(receivedContext).isSameAs(context)
    assertThat(lifecycle.getPhase()).isEqualTo(customPhase)
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
  fun `rejects registrations while transitioning or after their phase`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    lifecycle.register(participant(ProcessApplicationPhase.INFRASTRUCTURE_READY) {
      assertThatIllegalStateException().isThrownBy {
        lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
      }.withMessage("A lifecycle transition is already in progress")
      assertThatIllegalStateException().isThrownBy {
        lifecycle.register(participant(ProcessApplicationPhase.RUNNING) {})
      }.withMessage("Lifecycle participants can only be registered for a phase after NOT_STARTED (-2147483648)")
    })

    lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)

    assertThatIllegalStateException().isThrownBy {
      lifecycle.register(participant(ProcessApplicationPhase.INFRASTRUCTURE_READY) {})
    }.withMessage("Lifecycle participants can only be registered for a phase after INFRASTRUCTURE_READY (-2147482648)")
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
  fun `blocks startup but permits shutdown when a startup participant fails`(output: CapturedOutput) {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<String>()
    lifecycle.register(participant(ProcessApplicationPhase.INFRASTRUCTURE_READY) {
      throw IllegalStateException("registration failed")
    })
    lifecycle.register(participant(ProcessApplicationPhase.RUNNING) { calls += "running" })
    lifecycle.register(participant(ProcessApplicationPhase.STOPPING) { context ->
      calls += "stopping"
      assertThat(context.getStartupFailure()).hasMessage("registration failed")
    })
    lifecycle.register(participant(ProcessApplicationPhase.STOPPED) { context ->
      calls += "stopped"
      assertThat(context.getStartupFailure()).hasMessage("registration failed")
    })

    assertThatIllegalStateException().isThrownBy {
      lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    }.withMessage("registration failed")

    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.NOT_STARTED)
    assertThat(output.all).contains(
      "PROCESS-ENGINE-API-LIFECYCLE-003: Lifecycle phase " +
        "INFRASTRUCTURE_READY (${ProcessApplicationPhase.INFRASTRUCTURE_READY}) participant"
    )
    assertThatIllegalStateException().isThrownBy {
      lifecycle.transitionTo(ProcessApplicationPhase.INFRASTRUCTURE_READY)
    }.withMessage("Lifecycle is unusable after a participant failed during startup")
      .withCauseInstanceOf(IllegalStateException::class.java)

    lifecycle.transitionTo(ProcessApplicationPhase.STOPPED)

    assertThat(calls).containsExactly("stopping", "stopped")
    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.STOPPED)
  }

  @Test
  fun `continues shutdown after participant failures and reports them together`() {
    val lifecycle = DefaultProcessApplicationLifecycle()
    val calls = mutableListOf<String>()
    lifecycle.register(participant(ProcessApplicationPhase.STOPPING) {
      calls += "failing-stop"
      throw IllegalStateException("stop failed")
    })
    lifecycle.register(participant(ProcessApplicationPhase.STOPPING) { calls += "second-stop" })
    lifecycle.register(participant(ProcessApplicationPhase.STOPPED) { calls += "stopped" })

    lifecycle.transitionTo(
      ProcessApplicationPhase.RUNNING,
      TaskSubscriptionRegistrationContext(taskSubscriptionApi)
    )

    val failure = catchThrowable {
      lifecycle.transitionTo(ProcessApplicationPhase.STOPPED)
    }

    assertThat(failure).isInstanceOf(IllegalStateException::class.java)
      .hasMessage("One or more lifecycle participants failed during shutdown")
    assertThat(failure!!.suppressed).hasSize(1)
    assertThat(failure.suppressed.single().message).isEqualTo("stop failed")

    assertThat(calls).containsExactly("failing-stop", "second-stop", "stopped")
    assertThat(lifecycle.getPhase()).isEqualTo(ProcessApplicationPhase.STOPPED)
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

internal class CapturedOutput(private val output: ByteArrayOutputStream) {
  val all: String
    get() = output.toString(StandardCharsets.UTF_8)
}

internal class StandardOutputCaptureExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private lateinit var originalOut: PrintStream
  private lateinit var originalErr: PrintStream
  private lateinit var capturedOutput: CapturedOutput

  override fun beforeEach(context: ExtensionContext) {
    val output = ByteArrayOutputStream()
    capturedOutput = CapturedOutput(output)
    originalOut = System.out
    originalErr = System.err
    val stream = PrintStream(output, true, StandardCharsets.UTF_8)
    System.setOut(stream)
    System.setErr(stream)
  }

  override fun afterEach(context: ExtensionContext) {
    System.setOut(originalOut)
    System.setErr(originalErr)
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
    parameterContext.parameter.type == CapturedOutput::class.java

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any = capturedOutput
}
