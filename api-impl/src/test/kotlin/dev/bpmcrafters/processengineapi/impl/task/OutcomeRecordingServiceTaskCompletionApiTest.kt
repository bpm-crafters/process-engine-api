package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerInterceptor
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

internal class OutcomeRecordingServiceTaskCompletionApiTest {

  private val taskInformation = TaskInformation("task-id", emptyMap())
  private val delegatedCommands = mutableListOf<Any>()
  private val completionApi = OutcomeRecordingServiceTaskCompletionApi(object : ServiceTaskCompletionApi {
    override fun completeTask(cmd: CompleteTaskCmd) = delegated(cmd)
    override fun completeTaskByError(cmd: CompleteTaskByErrorCmd) = delegated(cmd)
    override fun failTask(cmd: FailTaskCmd) = delegated(cmd)
  })

  private fun delegated(cmd: Any): CompletableFuture<Empty> {
    delegatedCommands += cmd
    return CompletableFuture.completedFuture(Empty)
  }

  private fun outcomeSeenByInterceptorOf(handler: TaskHandler): TaskHandlerOutcome {
    var seenOutcome: TaskHandlerOutcome? = null
    val interceptor = TaskHandlerInterceptor { _, chain -> chain.proceed().also { seenOutcome = it } }
    InterceptingTaskHandler(handler, listOf(interceptor), "task-key", TaskType.EXTERNAL)
      .accept(taskInformation, emptyMap())
    return seenOutcome!!
  }

  @Test
  fun `reports failed task`() {
    val cmd = FailTaskCmd("task-id", "reason", "details")

    val outcome = outcomeSeenByInterceptorOf { _, _ -> completionApi.failTask(cmd).get() }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Failed("reason", "details"))
    assertThat(delegatedCommands).containsExactly(cmd)
  }

  @Test
  fun `reports task completed by error`() {
    val cmd = CompleteTaskByErrorCmd("task-id", "error-code", "message")

    val outcome = outcomeSeenByInterceptorOf { _, _ -> completionApi.completeTaskByError(cmd).get() }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.CompletedByError("error-code", "message"))
    assertThat(delegatedCommands).containsExactly(cmd)
  }

  @Test
  fun `reports completed task`() {
    val cmd = CompleteTaskCmd("task-id") { emptyMap() }

    val outcome = outcomeSeenByInterceptorOf { _, _ -> completionApi.completeTask(cmd).get() }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Completed)
    assertThat(delegatedCommands).containsExactly(cmd)
  }

  @Test
  fun `reports undetermined outcome if handler does not finish the task`() {
    val outcome = outcomeSeenByInterceptorOf { _, _ -> }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `ignores outcome of another task`() {
    val outcome = outcomeSeenByInterceptorOf { _, _ ->
      completionApi.failTask(FailTaskCmd("other-task-id", "reason", null)).get()
    }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `ignores outcome reported by another thread`() {
    val outcome = outcomeSeenByInterceptorOf { _, _ ->
      CompletableFuture.runAsync { completionApi.failTask(FailTaskCmd("task-id", "reason", null)).get() }.get()
    }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `does not leak outcome of a throwing handler into the next execution`() {
    assertThatThrownBy {
      outcomeSeenByInterceptorOf { _, _ ->
        completionApi.failTask(FailTaskCmd("task-id", "reason", null)).get()
        throw IllegalStateException("boom")
      }
    }.isInstanceOf(IllegalStateException::class.java)

    assertThat(outcomeSeenByInterceptorOf { _, _ -> }).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `delegates without a running handler execution`() {
    val cmd = FailTaskCmd("task-id", "reason", null)

    completionApi.failTask(cmd).get()

    assertThat(delegatedCommands).containsExactly(cmd)
  }
}
