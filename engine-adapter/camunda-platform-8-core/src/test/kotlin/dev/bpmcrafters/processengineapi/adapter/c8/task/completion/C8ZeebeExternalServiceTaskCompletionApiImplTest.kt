package dev.bpmcrafters.processengineapi.adapter.c8.task.completion

import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.ZeebeFuture
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1
import io.camunda.zeebe.client.api.command.FailJobCommandStep1
import io.camunda.zeebe.client.api.command.FailJobCommandStep1.FailJobCommandStep2
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1.ThrowErrorCommandStep2
import io.camunda.zeebe.client.api.response.CompleteJobResponse
import io.camunda.zeebe.client.api.response.FailJobResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration


class C8ZeebeExternalServiceTaskCompletionApiImplTest {

  companion object {
    const val TASK_ID = "1234"
    const val REASON = "because"
    const val RETRIES = 1
    const val BACKOFF = 2L
    const val ERROR_CODE = "0815"
    val PAYLOAD = mapOf("foo" to "bar")
  }

  private val zeebeClient: ZeebeClient = mockk(relaxUnitFun = true)
  private val subscriptionRepository: SubscriptionRepository = mockk(relaxed = true)
  private val failureRetrySupplier: FailureRetrySupplier = mockk(relaxed = true)

  private val taskCompletionApi = C8ZeebeExternalServiceTaskCompletionApiImpl(
    zeebeClient,
    subscriptionRepository,
    failureRetrySupplier
  )

  @Test
  fun `complete task in Zeebe`() {
    // GIVEN
    val completeJobCommandStep1: CompleteJobCommandStep1 = mockk()
    val zeebeFuture: ZeebeFuture<CompleteJobResponse> = mockk(relaxed = true)

    every { failureRetrySupplier.apply(any()) } returns FailureRetrySupplier.FailureRetry(RETRIES, BACKOFF)
    every { zeebeClient.newCompleteCommand(TASK_ID.toLong()) } returns completeJobCommandStep1
    every { completeJobCommandStep1.variables(PAYLOAD) } returns completeJobCommandStep1
    every { completeJobCommandStep1.send() } returns zeebeFuture

    // WHEN
    taskCompletionApi.completeTask(
      CompleteTaskCmd(
        taskId = TASK_ID,
         payloadSupplier = { PAYLOAD}
      )
    )

    verify { completeJobCommandStep1.variables(PAYLOAD) }
    verify { completeJobCommandStep1.send() }
  }

  @Test
  fun `fail task in Zeebe`() {
    // GIVEN
    val failJobCommandStep1: FailJobCommandStep1 = mockk()
    val failJobCommandStep2: FailJobCommandStep2 = mockk()
    val zeebeFuture: ZeebeFuture<FailJobResponse> = mockk(relaxed = true)

    every { failureRetrySupplier.apply(any()) } returns FailureRetrySupplier.FailureRetry(RETRIES, BACKOFF)
    every { zeebeClient.newFailCommand(TASK_ID.toLong()) } returns failJobCommandStep1
    every { failJobCommandStep1.retries(any()) } returns failJobCommandStep2
    every { failJobCommandStep2.retryBackoff(any()) } returns failJobCommandStep2
    every { failJobCommandStep2.errorMessage(any()) } returns failJobCommandStep2
    every { failJobCommandStep2.send() } returns zeebeFuture

    // WHEN
    taskCompletionApi.failTask(
      FailTaskCmd(
        taskId = TASK_ID,
        reason = REASON,
        errorDetails = null
      )
    )

    // THEN
    verify { failJobCommandStep1.retries(RETRIES) }
    verify { failJobCommandStep2.retryBackoff(Duration.ofSeconds(BACKOFF)) }
    verify { failJobCommandStep2.errorMessage(REASON) }
    verify { failJobCommandStep2.send() }
  }

  @Test
  fun `complete task by error in Zeebe`() {
    // GIVEN
    val throwErrorCommandStep1: ThrowErrorCommandStep1 = mockk()
    val throwErrorCommandStep2: ThrowErrorCommandStep2 = mockk()
    val zeebeFuture: ZeebeFuture<Void> = mockk(relaxed = true)

    every { zeebeClient.newThrowErrorCommand(TASK_ID.toLong()) } returns throwErrorCommandStep1
    every { throwErrorCommandStep1.errorCode(any()) } returns throwErrorCommandStep2
    every { throwErrorCommandStep2.errorMessage(any()) } returns throwErrorCommandStep2
    every { throwErrorCommandStep2.variables(PAYLOAD) } returns throwErrorCommandStep2
    every { throwErrorCommandStep2.send() } returns zeebeFuture

    // WHEN
    taskCompletionApi.completeTaskByError(
      CompleteTaskByErrorCmd(
        taskId = TASK_ID,
        errorCode = ERROR_CODE,
        errorMessage = REASON,
        payloadSupplier = { PAYLOAD }
      )
    )

    // THEN
    verify { throwErrorCommandStep1.errorCode(ERROR_CODE) }
    verify { throwErrorCommandStep2.errorMessage(REASON) }
    verify { throwErrorCommandStep2.variables(PAYLOAD) }
    verify { throwErrorCommandStep2.send() }
  }
}
