package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskExecutionOutcome
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

internal class ObservedServiceTaskCompletionApiTest {

  private val recordedSteps = mutableListOf<String>()

  private val delegate = object : ServiceTaskCompletionApi {
    override fun completeTask(cmd: CompleteTaskCmd) = delegated("complete ${cmd.taskId}")
    override fun completeTaskByError(cmd: CompleteTaskByErrorCmd) = delegated("error ${cmd.taskId}")
    override fun failTask(cmd: FailTaskCmd) = delegated("fail ${cmd.taskId}")

    private fun delegated(step: String): CompletableFuture<Empty> {
      recordedSteps.add(step)
      return CompletableFuture.completedFuture(Empty)
    }
  }

  private val completionApi = ObservedServiceTaskCompletionApi(delegate, listOf(RecordingObserver("observer", recordedSteps)))

  @Test
  fun `reports completion before delegating`() {
    completionApi.completeTask(CompleteTaskCmd("task-1", mapOf()))

    assertThat(recordedSteps).containsExactly("observer outcome task-1 ${TaskExecutionOutcome.Completed}", "complete task-1")
  }

  @Test
  fun `reports bpmn error before delegating`() {
    completionApi.completeTaskByError(CompleteTaskByErrorCmd("task-1", "not-found", null))

    assertThat(recordedSteps).containsExactly("observer outcome task-1 ${TaskExecutionOutcome.BpmnError("not-found")}", "error task-1")
  }

  @Test
  fun `reports failure before delegating`() {
    completionApi.failTask(FailTaskCmd("task-1", "timeout", "details"))

    assertThat(recordedSteps).containsExactly("observer outcome task-1 ${TaskExecutionOutcome.Failed("timeout", "details")}", "fail task-1")
  }

  @Test
  fun `delegates even if an observer throws`() {
    val failingObserver = object : TaskExecutionObserver {
      override fun onOutcome(taskId: String, outcome: TaskExecutionOutcome) = throw IllegalStateException("outcome")
    }
    val completionApi = ObservedServiceTaskCompletionApi(delegate, listOf(failingObserver))

    completionApi.completeTask(CompleteTaskCmd("task-1", mapOf()))

    assertThat(recordedSteps).containsExactly("complete task-1")
  }
}
