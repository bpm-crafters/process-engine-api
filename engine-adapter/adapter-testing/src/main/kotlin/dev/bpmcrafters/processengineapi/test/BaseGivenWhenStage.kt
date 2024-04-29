package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageCmd
import dev.bpmcrafters.processengineapi.task.*
import io.mockk.mockk
import io.toolisticon.testing.jgiven.JGivenKotlinStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat

@JGivenKotlinStage
class BaseGivenWhenStage : Stage<BaseGivenWhenStage>() {

  @ExpectedScenarioState
  lateinit var processTestHelper: ProcessTestHelper

  @ProvidedScenarioState
  lateinit var instanceId: String

  @ProvidedScenarioState
  var userTaskId: String? = null

  @ProvidedScenarioState
  var externalTaskId: String? = null

  @ProvidedScenarioState
  lateinit var taskSubscription: TaskSubscription

  fun `start process by definition`(definitionKey: String) = step {
    instanceId = processTestHelper.getStartProcessApi().startProcess(
      StartProcessByDefinitionCmd(
        definitionKey = definitionKey,
        payloadSupplier = { emptyMap() }
      )
    ).get().instanceId
  }

  fun `start process by definition with payload`(definitionKey: String, singlePayload: Pair<String, Any>) = step {
    instanceId = processTestHelper.getStartProcessApi().startProcess(
      StartProcessByDefinitionCmd(
        definitionKey = definitionKey,
        payloadSupplier = { mapOf(singlePayload) }
      )
    ).get().instanceId
  }

  fun `start process by message`(messageName: String) = step {
    instanceId = processTestHelper.getStartProcessApi().startProcess(
      StartProcessByMessageCmd(
        messageName = messageName,
        payloadSupplier = { emptyMap() }
      )
    ).get().instanceId
  }

  fun `start process by message with payload`(messageName: String, singlePayload: Pair<String, Any>) = step {
    instanceId = processTestHelper.getStartProcessApi().startProcess(
      StartProcessByMessageCmd(
        messageName = messageName,
        payloadSupplier = { mapOf(singlePayload) }
      )
    ).get().instanceId
  }

  fun `a active user task subscription`(taskDescriptionKey: String) = step {
    taskSubscription = subscribeTask(TaskType.USER, taskDescriptionKey) { taskInformation, _ -> userTaskId = taskInformation.taskId }
  }

  fun `a active external task subscription`(taskDescriptionKey: String) = step {
    taskSubscription = subscribeTask(TaskType.EXTERNAL, taskDescriptionKey) { taskInformation, _ -> externalTaskId = taskInformation.taskId }
  }

  fun `complete the user task`() = step {
    assertThat(userTaskId).isNotEmpty()

    processTestHelper.getUserTaskCompletionApi().completeTask(
      CompleteTaskCmd(
        taskId = userTaskId!!,
        payloadSupplier = { emptyMap() }
      )
    )
  }

  fun `complete the external task`() = step {
    assertThat(externalTaskId).isNotEmpty()

    processTestHelper.getExternalTaskCompletionApi().completeTask(
      CompleteTaskCmd(
        taskId = externalTaskId!!,
        payloadSupplier = { emptyMap() }
      )
    )
  }

  fun `unsubscribe user task subscription`() = step { unsubscribeTask() }

  fun `unsubscribe external task subscription`() = step { unsubscribeTask() }

  private fun subscribeTask(taskType: TaskType, taskDescriptionKey: String, taskHandler: TaskHandler) = processTestHelper.getTaskSubscriptionApi().subscribeForTask(
    SubscribeForTaskCmd(
        restrictions = emptyMap(),
        taskType = taskType,
        taskDescriptionKey = taskDescriptionKey,
        action = taskHandler,
        termination = {} // nothing to do
      )
    ).get()

  private fun unsubscribeTask() = processTestHelper.getTaskSubscriptionApi().unsubscribe(
    UnsubscribeFromTaskCmd(
      taskSubscription
    )
  )

}
