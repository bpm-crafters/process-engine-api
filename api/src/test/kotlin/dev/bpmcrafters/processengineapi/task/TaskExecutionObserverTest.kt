package dev.bpmcrafters.processengineapi.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class TaskExecutionObserverTest {

  private val observerWithoutOverrides = object : TaskExecutionObserver {}

  @Test
  fun `starts without cleanup by default`() {
    val cleanup = observerWithoutOverrides.onStart(TaskInformation("task-1", mapOf()))

    assertThat(cleanup).isSameAs(TaskExecutionCleanup.NONE)
    assertThatCode { cleanup.close() }.doesNotThrowAnyException()
  }

  @Test
  fun `ignores outcomes by default`() {
    assertThatCode { observerWithoutOverrides.onOutcome("task-1", TaskExecutionOutcome.Completed) }.doesNotThrowAnyException()
  }

  @Test
  fun `outcomes carry their details`() {
    val cause = IllegalStateException("boom")

    assertThat(TaskExecutionOutcome.BpmnError("not-found").errorCode).isEqualTo("not-found")
    assertThat(TaskExecutionOutcome.Failed("timeout", "details")).isEqualTo(TaskExecutionOutcome.Failed("timeout", "details"))
    assertThat(TaskExecutionOutcome.Thrown(cause).cause).isSameAs(cause)
  }
}
