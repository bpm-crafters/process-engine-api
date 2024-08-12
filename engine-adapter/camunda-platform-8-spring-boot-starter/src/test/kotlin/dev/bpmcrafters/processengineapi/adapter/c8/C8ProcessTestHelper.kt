package dev.bpmcrafters.processengineapi.adapter.c8

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper

class C8ProcessTestHelper(
  private val startProcessApi: StartProcessApi,
  private val subscribingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery,
  private val pullUserTaskDelivery: PullUserTaskDelivery,
  private val subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery,
  private val taskSubscriptionApi: TaskSubscriptionApi,
  private val userTaskCompletionApi: UserTaskCompletionApi,
  private val externalTaskCompletionApi: ExternalTaskCompletionApi,
  private val subscriptionRepository: SubscriptionRepository
) : ProcessTestHelper {

  override fun getStartProcessApi(): StartProcessApi = startProcessApi
  override fun getTaskSubscriptionApi(): TaskSubscriptionApi = taskSubscriptionApi
  override fun getUserTaskCompletionApi(): UserTaskCompletionApi = userTaskCompletionApi
  override fun getExternalTaskCompletionApi(): ExternalTaskCompletionApi = externalTaskCompletionApi

  override fun triggerPullingUserTaskDeliveryManually() = pullUserTaskDelivery.refresh()
  override fun subscribeForUserTasks() = subscribingUserTaskDelivery.subscribe()

  override fun triggerExternalTaskDeliveryManually() = subscribingServiceTaskDelivery.subscribe()

  override fun getProcessInformation(instanceId: String): ProcessInformation = ProcessInformation(
    instanceId = "fixme",
    meta = emptyMap()
  )

  override fun clearAllSubscriptions() {
    (subscriptionRepository as InMemSubscriptionRepository).deleteAllTaskSubscriptions()
    subscribingUserTaskDelivery.unsubscribeAll()
  }

}
