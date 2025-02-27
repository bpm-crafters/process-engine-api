package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.toProcessInformation
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.RuntimeService

class C7EmbeddedSpringProcessTestHelper(
  private val runtimeService: RuntimeService,
  private val startProcessApi: StartProcessApi,
  private val userTaskDelivery: EmbeddedPullUserTaskDelivery,
  private val externalTaskDelivery: EmbeddedPullServiceTaskDelivery,
  private val taskSubscriptionApi: TaskSubscriptionApi,
  private val userTaskCompletionApi: UserTaskCompletionApi,
  private val serviceTaskCompletionApi: ServiceTaskCompletionApi,
  private val subscriptionRepository: SubscriptionRepository
) : ProcessTestHelper {

  override fun getStartProcessApi(): StartProcessApi = startProcessApi
  override fun getTaskSubscriptionApi(): TaskSubscriptionApi = taskSubscriptionApi
  override fun getUserTaskCompletionApi(): UserTaskCompletionApi = userTaskCompletionApi
  override fun getServiceTaskCompletionApi(): ServiceTaskCompletionApi = serviceTaskCompletionApi

  override fun triggerPullingUserTaskDeliveryManually() = userTaskDelivery.refresh()
  override fun subscribeForUserTasks() {
    TODO("Not yet implemented")
  }

  override fun triggerExternalTaskDeliveryManually() = externalTaskDelivery.refresh()

  override fun getProcessInformation(instanceId: String): ProcessInformation =
    runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(instanceId)
      .singleResult()
      .toProcessInformation()

  override fun clearAllSubscriptions() = (subscriptionRepository as InMemSubscriptionRepository).deleteAllTaskSubscriptions()

}
