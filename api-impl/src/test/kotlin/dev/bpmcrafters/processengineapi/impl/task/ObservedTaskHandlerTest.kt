package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskExecutionCleanup
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ObservedTaskHandlerTest {

  private val recordedSteps = mutableListOf<String>()
  private val task = TaskInformation("task-1", mapOf())

  @Test
  fun `runs cleanups after the handler in reverse order`() {
    val handler = ObservedTaskHandler(
      { task, _ -> recordedSteps.add("handler ${task.taskId}") },
      listOf(RecordingObserver("first", recordedSteps), RecordingObserver("second", recordedSteps))
    )

    handler.accept(task, mapOf())

    assertThat(recordedSteps).containsExactly(
      "first start task-1",
      "second start task-1",
      "handler task-1",
      "second cleanup task-1",
      "first cleanup task-1"
    )
  }

  @Test
  fun `reports an escaping exception before the cleanup and rethrows it unchanged`() {
    val failure = IllegalStateException("boom")
    val handler = ObservedTaskHandler({ _, _ -> throw failure }, listOf(RecordingObserver("observer", recordedSteps)))

    assertThatThrownBy { handler.accept(task, mapOf()) }.isSameAs(failure)
    assertThat(recordedSteps).containsExactly(
      "observer start task-1",
      "observer outcome task-1 ${TaskExecutionOutcome.Thrown(failure)}",
      "observer cleanup task-1"
    )
  }

  @Test
  fun `executes the handler once even if every observer callback throws`() {
    val failingObserver = object : TaskExecutionObserver {
      override fun onStart(task: TaskInformation): TaskExecutionCleanup = throw IllegalStateException("start")
      override fun onOutcome(taskId: String, outcome: TaskExecutionOutcome) = throw IllegalStateException("outcome")
    }
    val failingCleanupObserver = object : TaskExecutionObserver {
      override fun onStart(task: TaskInformation) = TaskExecutionCleanup { throw IllegalStateException("close") }
    }
    val handler = ObservedTaskHandler(
      { _, _ -> recordedSteps.add("handler") },
      listOf(failingObserver, failingCleanupObserver, RecordingObserver("healthy", recordedSteps))
    )

    handler.accept(task, mapOf())

    assertThat(recordedSteps).containsExactly("healthy start task-1", "handler", "healthy cleanup task-1")
  }

  @Test
  fun `observer without overrides does nothing`() {
    val handler = ObservedTaskHandler({ _, _ -> recordedSteps.add("handler") }, listOf(object : TaskExecutionObserver {}))

    handler.accept(task, mapOf())

    assertThat(recordedSteps).containsExactly("handler")
  }

  @Test
  fun `reports an error escaping the handler`() {
    val failure = StackOverflowError("deep")
    val handler = ObservedTaskHandler({ _, _ -> throw failure }, listOf(RecordingObserver("observer", recordedSteps)))

    assertThatThrownBy { handler.accept(task, mapOf()) }.isSameAs(failure)
    assertThat(recordedSteps).contains("observer outcome task-1 ${TaskExecutionOutcome.Thrown(failure)}")
  }

  @Test
  fun `isolates errors thrown by an observer`() {
    val unfinishedObserver = object : TaskExecutionObserver {
      override fun onStart(task: TaskInformation): TaskExecutionCleanup = TODO()
    }
    val handler = ObservedTaskHandler({ _, _ -> recordedSteps.add("handler") }, listOf(unfinishedObserver))

    handler.accept(task, mapOf())

    assertThat(recordedSteps).containsExactly("handler")
  }

  @Test
  fun `keeps the cleanup order of healthy observers when an earlier observer fails to start`() {
    val failingObserver = object : TaskExecutionObserver {
      override fun onStart(task: TaskInformation): TaskExecutionCleanup = throw IllegalStateException("start")
    }
    val handler = ObservedTaskHandler(
      { _, _ -> recordedSteps.add("handler") },
      listOf(failingObserver, RecordingObserver("first", recordedSteps), RecordingObserver("second", recordedSteps))
    )

    handler.accept(task, mapOf())

    assertThat(recordedSteps).containsExactly(
      "first start task-1",
      "second start task-1",
      "handler",
      "second cleanup task-1",
      "first cleanup task-1"
    )
  }

  @Test
  fun `reports both outcomes when the handler fails the task and then throws`() {
    val observers = listOf(RecordingObserver("observer", recordedSteps))
    val completionApi = ObservedServiceTaskCompletionApi(NoOpServiceTaskCompletionApi(), observers)
    val failure = IllegalStateException("boom")
    val handler = ObservedTaskHandler(
      { task, _ ->
        completionApi.failTask(FailTaskCmd(task.taskId, "timeout", null))
        throw failure
      },
      observers
    )

    assertThatThrownBy { handler.accept(task, mapOf()) }.isSameAs(failure)
    assertThat(recordedSteps).containsExactly(
      "observer start task-1",
      "observer outcome task-1 ${TaskExecutionOutcome.Failed("timeout", null)}",
      "observer outcome task-1 ${TaskExecutionOutcome.Thrown(failure)}",
      "observer cleanup task-1"
    )
  }

  @Test
  fun `does not swallow fatal errors of an observer`() {
    val fatalObserver = object : TaskExecutionObserver {
      override fun onStart(task: TaskInformation): TaskExecutionCleanup = throw OutOfMemoryError("fatal")
    }
    val handler = ObservedTaskHandler({ _, _ -> recordedSteps.add("handler") }, listOf(fatalObserver))

    assertThatThrownBy { handler.accept(task, mapOf()) }.isInstanceOf(OutOfMemoryError::class.java)
    assertThat(recordedSteps).isEmpty()
  }
}
