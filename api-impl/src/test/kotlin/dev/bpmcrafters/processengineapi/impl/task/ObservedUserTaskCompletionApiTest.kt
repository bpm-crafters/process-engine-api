package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import dev.bpmcrafters.processengineapi.task.TaskType
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

internal class ObservedUserTaskCompletionApiTest {

  private val recordedSteps = mutableListOf<String>()

  private val delegate = object : UserTaskCompletionApi {
    override fun completeTask(cmd: CompleteTaskCmd) = delegated("complete ${cmd.taskId}")
    override fun completeTaskByError(cmd: CompleteTaskByErrorCmd) = delegated("error ${cmd.taskId}")

    private fun delegated(step: String): CompletableFuture<Empty> {
      recordedSteps.add(step)
      return CompletableFuture.completedFuture(Empty)
    }
  }

  private val completionApi = ObservedUserTaskCompletionApi(delegate, listOf(RecordingObserver("observer", recordedSteps)))

  @Test
  fun `reports completion of a user task before delegating`() {
    completionApi.completeTask(CompleteTaskCmd("task-1", mapOf()))

    assertThat(recordedSteps).containsExactly("observer outcome USER task-1 ${TaskExecutionOutcome.Completed}", "complete task-1")
  }

  @Test
  fun `reports bpmn error of a user task before delegating`() {
    completionApi.completeTaskByError(CompleteTaskByErrorCmd("task-1", "rejected", null))

    assertThat(recordedSteps).containsExactly("observer outcome USER task-1 ${TaskExecutionOutcome.BpmnError("rejected")}", "error task-1")
  }

  @Test
  fun `delegates even if an observer throws`() {
    val failingObserver = object : TaskExecutionObserver {
      override fun onOutcome(taskId: String, taskType: TaskType, outcome: TaskExecutionOutcome) = throw IllegalStateException("outcome")
    }
    val completionApi = ObservedUserTaskCompletionApi(delegate, listOf(failingObserver))

    completionApi.completeTask(CompleteTaskCmd("task-1", mapOf()))

    assertThat(recordedSteps).containsExactly("complete task-1")
  }
}
