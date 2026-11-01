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

internal class InterceptorAwareServiceTaskCompletionApiTest {

  private val taskInformation = TaskInformation("task-id", emptyMap())
  private val delegatedCommands = mutableListOf<Any>()
  private val completionApi = InterceptorAwareServiceTaskCompletionApi(object : ServiceTaskCompletionApi {
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
  fun `does not report outcome if the engine call throws`() {
    val failingApi = InterceptorAwareServiceTaskCompletionApi(object : ServiceTaskCompletionApi {
      override fun completeTask(cmd: CompleteTaskCmd): CompletableFuture<Empty> = throw IllegalStateException("boom")
      override fun completeTaskByError(cmd: CompleteTaskByErrorCmd) = delegated(cmd)
      override fun failTask(cmd: FailTaskCmd) = delegated(cmd)
    })

    val outcome = outcomeSeenByInterceptorOf { _, _ ->
      runCatching { failingApi.completeTask(CompleteTaskCmd("task-id") { emptyMap() }) }
    }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `reports outcome regardless of the result of the returned future`() {
    val pendingApi = InterceptorAwareServiceTaskCompletionApi(object : ServiceTaskCompletionApi {
      override fun completeTask(cmd: CompleteTaskCmd) = CompletableFuture<Empty>()
      override fun completeTaskByError(cmd: CompleteTaskByErrorCmd) = delegated(cmd)
      override fun failTask(cmd: FailTaskCmd) = delegated(cmd)
    })

    val outcome = outcomeSeenByInterceptorOf { _, _ ->
      pendingApi.completeTask(CompleteTaskCmd("task-id") { emptyMap() })
    }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Completed)
  }

  @Test
  fun `keeps outcome of enclosing execution after a nested execution`() {
    val outcome = outcomeSeenByInterceptorOf { _, _ ->
      completionApi.failTask(FailTaskCmd("task-id", "reason", null))
      outcomeSeenByInterceptorOf { _, _ -> }
    }

    assertThat(outcome).isEqualTo(TaskHandlerOutcome.Failed("reason", null))
  }

  @Test
  fun `does not report outcome of a previous attempt if interceptor proceeds twice`() {
    var attempts = 0
    val retryingInterceptor = TaskHandlerInterceptor { _, chain ->
      chain.proceed()
      chain.proceed()
    }
    var seenOutcome: TaskHandlerOutcome? = null
    val observingInterceptor = TaskHandlerInterceptor { _, chain -> chain.proceed().also { seenOutcome = it } }
    val handler = TaskHandler { _, _ ->
      if (attempts++ == 0) completionApi.failTask(FailTaskCmd("task-id", "reason", null))
    }

    InterceptingTaskHandler(handler, listOf(retryingInterceptor, observingInterceptor), "task-key", TaskType.EXTERNAL)
      .accept(taskInformation, emptyMap())

    assertThat(seenOutcome).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `does not report outcome of a previous attempt if inner interceptor skips the handler`() {
    var attempts = 0
    var secondOutcome: TaskHandlerOutcome? = null
    val retryingInterceptor = TaskHandlerInterceptor { _, chain ->
      chain.proceed()
      secondOutcome = chain.proceed()
    }
    val skippingInterceptor = TaskHandlerInterceptor { _, chain -> if (attempts++ == 0) chain.proceed() }
    val handler = TaskHandler { _, _ -> completionApi.failTask(FailTaskCmd("task-id", "reason", null)) }

    InterceptingTaskHandler(handler, listOf(retryingInterceptor, skippingInterceptor), "task-key", TaskType.EXTERNAL)
      .accept(taskInformation, emptyMap())

    assertThat(secondOutcome).isEqualTo(TaskHandlerOutcome.Undetermined)
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

    assertThat(TaskHandlerOutcomeRecorder.recordedOutcome()).isEqualTo(TaskHandlerOutcome.Undetermined)
  }

  @Test
  fun `executes command without a running handler execution`() {
    val cmd = FailTaskCmd("task-id", "reason", null)

    completionApi.failTask(cmd).get()

    assertThat(delegatedCommands).containsExactly(cmd)
  }
}
