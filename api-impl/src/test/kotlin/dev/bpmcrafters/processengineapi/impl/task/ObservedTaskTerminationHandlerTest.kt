package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ObservedTaskTerminationHandlerTest {

  private val recordedSteps = mutableListOf<String>()
  private val task = TaskInformation("task-1", mapOf())

  @Test
  fun `reports the termination before calling the handler`() {
    val handler = ObservedTaskTerminationHandler(
      { task -> recordedSteps.add("terminated ${task.taskId}") },
      TaskType.USER,
      listOf(RecordingObserver("observer", recordedSteps))
    )

    handler.accept(task)

    assertThat(recordedSteps).containsExactly("observer termination USER task-1", "terminated task-1")
  }

  @Test
  fun `calls the handler even if an observer throws`() {
    val failingObserver = object : TaskExecutionObserver {
      override fun onTermination(task: TaskInformation, taskType: TaskType) = throw IllegalStateException("termination")
    }
    val handler = ObservedTaskTerminationHandler({ task -> recordedSteps.add("terminated ${task.taskId}") }, TaskType.USER, listOf(failingObserver))

    handler.accept(task)

    assertThat(recordedSteps).containsExactly("terminated task-1")
  }

  @Test
  fun `rethrows what the handler throws`() {
    val failure = IllegalStateException("boom")
    val handler = ObservedTaskTerminationHandler({ _ -> throw failure }, TaskType.EXTERNAL, listOf(RecordingObserver("observer", recordedSteps)))

    assertThatThrownBy { handler.accept(task) }.isSameAs(failure)
    assertThat(recordedSteps).containsExactly("observer termination EXTERNAL task-1")
  }
}
