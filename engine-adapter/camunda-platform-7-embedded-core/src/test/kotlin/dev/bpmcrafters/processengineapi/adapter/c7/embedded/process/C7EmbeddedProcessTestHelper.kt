package dev.bpmcrafters.processengineapi.adapter.c7.embedded.process

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7ServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7UserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.subscription.C7TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.impl.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.ProcessEngine
import java.util.concurrent.Executors

const val WORKER_ID = "execute-action-external"

class C7EmbeddedProcessTestHelper(private val processEngine: ProcessEngine) : ProcessTestHelper {

  private var subscriptionRepository: InMemSubscriptionRepository = InMemSubscriptionRepository()

  private var embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery = EmbeddedPullUserTaskDelivery(
    taskService = processEngine.taskService,
    subscriptionRepository = subscriptionRepository,
    repositoryService = processEngine.repositoryService,
    executorService = Executors.newFixedThreadPool(3)
  )

  private var embeddedPullExternalTaskDelivery: EmbeddedPullServiceTaskDelivery = EmbeddedPullServiceTaskDelivery(
    externalTaskService = processEngine.externalTaskService,
    workerId = WORKER_ID,
    subscriptionRepository = subscriptionRepository,
    maxTasks = 100,
    lockDurationInSeconds = 10L,
    retryTimeoutInSeconds = 10L,
    retries = 3,
    executorService = Executors.newFixedThreadPool(3)
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

  override fun getServiceTaskCompletionApi(): ServiceTaskCompletionApi = C7ServiceTaskCompletionApiImpl(
    workerId = WORKER_ID,
    externalTaskService = processEngine.externalTaskService,
    subscriptionRepository = subscriptionRepository,
    failureRetrySupplier = LinearMemoryFailureRetrySupplier(
      retry = 1,
      retryTimeout = 10
    )
  )

  override fun triggerPullingUserTaskDeliveryManually() = embeddedPullUserTaskDelivery.refresh()

  override fun subscribeForUserTasks() {
    TODO("Not yet implemented")
  }

  override fun triggerExternalTaskDeliveryManually() = embeddedPullExternalTaskDelivery.refresh()

  override fun getProcessInformation(instanceId: String): ProcessInformation =
    processEngine.runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(instanceId)
      .singleResult()
      .toProcessInformation()

  override fun clearAllSubscriptions() = subscriptionRepository.deleteAllTaskSubscriptions()


}
