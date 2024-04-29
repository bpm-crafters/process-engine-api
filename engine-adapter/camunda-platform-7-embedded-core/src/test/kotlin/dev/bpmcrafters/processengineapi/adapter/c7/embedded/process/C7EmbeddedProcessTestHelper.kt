package dev.bpmcrafters.processengineapi.adapter.c7.embedded.process

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7ExternalTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7UserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.subscription.C7TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.ProcessEngine

const val WORKER_ID = "execute-action-external"

class C7EmbeddedProcessTestHelper(private val processEngine: ProcessEngine) : ProcessTestHelper {

  private var subscriptionRepository: InMemSubscriptionRepository = InMemSubscriptionRepository()

  private var embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery = EmbeddedPullUserTaskDelivery(
    taskService = processEngine.taskService,
    subscriptionRepository = subscriptionRepository
  )

  private var embeddedPullExternalTaskDelivery: EmbeddedPullExternalTaskDelivery = EmbeddedPullExternalTaskDelivery(
    externalTaskService = processEngine.externalTaskService,
    workerId = WORKER_ID,
    subscriptionRepository = subscriptionRepository,
    maxTasks = 100,
    lockDuration = 10L,
    retryTimeout = 10L
  )

  override fun getStartProcessApi(): StartProcessApi = StartProcessApiImpl(
    runtimeService = processEngine.runtimeService
  )

  override fun getTaskSubscriptionApi(): TaskSubscriptionApi = C7TaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  override fun getUserTaskCompletionApi(): UserTaskCompletionApi = C7UserTaskCompletionApiImpl(
    taskService = processEngine.taskService,
    subscriptionRepository = subscriptionRepository
  )

  override fun getExternalTaskCompletionApi(): ExternalTaskCompletionApi = C7ExternalTaskCompletionApiImpl(
    workerId = WORKER_ID,
    externalTaskService = processEngine.externalTaskService,
    subscriptionRepository = subscriptionRepository
  )

  override fun triggerUserTaskDeliveryManually() = embeddedPullUserTaskDelivery.deliverAll()

  override fun triggerExternalTaskDeliveryManually() = embeddedPullExternalTaskDelivery.deliverAll()

  override fun getProcessInformation(instanceId: String): ProcessInformation =
    processEngine.runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(instanceId)
      .singleResult()
      .toProcessInformation()

  override fun clearAllSubscriptions() = subscriptionRepository.deleteAllTaskSubscriptions()


}
