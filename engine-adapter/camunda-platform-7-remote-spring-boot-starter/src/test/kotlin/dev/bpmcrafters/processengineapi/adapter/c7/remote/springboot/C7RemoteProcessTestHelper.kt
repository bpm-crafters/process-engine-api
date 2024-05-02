package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.process.toProcessInformation
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RuntimeService

class C7RemoteProcessTestHelper(
  private val runtimeService: RuntimeService,
  private val startProcessApi: StartProcessApi,
  private val userTaskDelivery: UserTaskDelivery,
  private val externalTaskDelivery: RemotePullExternalTaskDelivery,
  private val taskSubscriptionApi: TaskSubscriptionApi,
  private val userTaskCompletionApi: UserTaskCompletionApi,
  private val externalTaskCompletionApi: ExternalTaskCompletionApi,
  private val subscriptionRepository: SubscriptionRepository
) : ProcessTestHelper {

  override fun getStartProcessApi(): StartProcessApi = startProcessApi
  override fun getTaskSubscriptionApi(): TaskSubscriptionApi = taskSubscriptionApi
  override fun getUserTaskCompletionApi(): UserTaskCompletionApi = userTaskCompletionApi
  override fun getExternalTaskCompletionApi(): ExternalTaskCompletionApi = externalTaskCompletionApi

  override fun triggerPullingUserTaskDeliveryManually() = userTaskDelivery.deliverAll()
  override fun subscribeForUserTasks() {
    TODO("Not yet implemented")
  }

  override fun triggerExternalTaskDeliveryManually() = externalTaskDelivery.deliverAll()

  override fun getProcessInformation(instanceId: String): ProcessInformation =
    runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(instanceId)
      .singleResult()
      .toProcessInformation()

  override fun clearAllSubscriptions() = (subscriptionRepository as InMemSubscriptionRepository).deleteAllTaskSubscriptions()

}
