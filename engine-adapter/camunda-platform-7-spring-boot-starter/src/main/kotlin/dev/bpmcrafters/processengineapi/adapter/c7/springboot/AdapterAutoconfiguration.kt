package dev.bpmcrafters.processengineapi.adapter.c7.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.TaskApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.ExternalTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.ExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskApi
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*

@Configuration
@EnableScheduling
class AdapterAutoconfiguration {

  companion object {
    val WORKER_ID: String = UUID.randomUUID().toString() // FIXME: move to props
  }

  @Bean
  fun startProcessApi(runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun taskApi(subscriptionRepository: SubscriptionRepository, completionStrategies: List<CompletionStrategy>): TaskApi = TaskApiImpl(
    completionStrategies = completionStrategies,
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  fun correlationApi(runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  @Bean
  fun externalTaskCompletionStrategy(externalTaskService: ExternalTaskService, subscriptionRepository: SubscriptionRepository): CompletionStrategy = ExternalTaskCompletionStrategy(
    workerId = WORKER_ID,
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  fun userTaskCompletionStrategy(taskService: TaskService, subscriptionRepository: SubscriptionRepository): CompletionStrategy = UserTaskCompletionStrategy(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository
  )

  @Bean // FIXME -> configurable on props
  fun deliveryScheduler(userTaskDelivery: UserTaskDelivery, externalTaskDelivery: ExternalTaskDelivery) = ScheduledDelivery(
    externalTaskDelivery = externalTaskDelivery,
    userTaskDelivery = userTaskDelivery
  )

  @Bean
  fun userTaskDelivery(subscriptionRepository: SubscriptionRepository, taskService: TaskService) = UserTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    taskService = taskService
  )

  @Bean
  fun externalTaskDelivery(subscriptionRepository: SubscriptionRepository, externalTaskService: ExternalTaskService) = ExternalTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = WORKER_ID
  )

}
