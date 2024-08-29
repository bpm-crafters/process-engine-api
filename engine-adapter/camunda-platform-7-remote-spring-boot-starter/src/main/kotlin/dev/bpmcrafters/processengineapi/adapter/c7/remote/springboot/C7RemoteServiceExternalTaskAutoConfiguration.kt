package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule.ScheduledEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule.ScheduledRemoteExternalServiceTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteServiceServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteServiceUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.ExecutorService

/**
 * Auto-configuration for scheduled delivery.
 */
@Configuration
@EnableScheduling
@AutoConfigureAfter(C7RemoteServiceApiAutoConfiguration::class)
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7RemoteServiceExternalTaskAutoConfiguration {

  @Bean("c7remote-service-task-completion-api")
  @Qualifier("c7remote-service-task-completion-api")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun serviceTaskCompletionApi(
    @Qualifier("remote") externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7RemoteAdapterProperties,
    @Qualifier("c7remote-failure-retry-supplier")
    failureRetrySupplier: FailureRetrySupplier
  ): ServiceTaskCompletionApi =
    C7RemoteServiceServiceTaskCompletionApiImpl(
      workerId = c7AdapterProperties.serviceTasks.workerId,
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository,
      failureRetrySupplier = failureRetrySupplier
    )

  @Bean("c7remote-user-task-completion-api")
  @Qualifier("c7remote-user-task-completion-api")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun userTaskCompletionApi(
    @Qualifier("remote") taskService: TaskService,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi =
    C7RemoteServiceUserTaskCompletionApiImpl(
      taskService = taskService,
      subscriptionRepository = subscriptionRepository
    )


  @Bean("c7remote-user-task-delivery")
  @Qualifier("c7remote-user-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun scheduledUserTaskDelivery(
    @Qualifier("remote") taskService: TaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7RemoteAdapterProperties,
    @Qualifier("remote") repositoryService: RepositoryService,
    @Qualifier("c7remote-user-task-worker-executor") executorService: ExecutorService,
  ): UserTaskDelivery {
    return RemotePullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskService = taskService,
      repositoryService = repositoryService,
      executorService = executorService
    )
  }

  @Bean("c7remote-service-task-delivery")
  @Qualifier("c7remote-service-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun scheduledExternalTaskDelivery(
    @Qualifier("remote") externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7RemoteAdapterProperties,
    @Qualifier("c7remote-user-task-worker-executor") executorService: ExecutorService,
  ) = RemotePullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.serviceTasks.workerId,
    maxTasks = c7AdapterProperties.serviceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = c7AdapterProperties.serviceTasks.retries,
    executorService = executorService
  )

  @Bean("c7remote-service-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun remoteScheduledExternalServiceTaskDeliveryBinding(
    @Qualifier("c7remote-service-task-delivery")
    remotePullServiceTaskDelivery: RemotePullServiceTaskDelivery
  ): ScheduledRemoteExternalServiceTaskDeliveryBinding {
    return ScheduledRemoteExternalServiceTaskDeliveryBinding(
      externalTaskDelivery = remotePullServiceTaskDelivery
    )
  }

  @Bean("c7remote-user-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun remoteScheduledUserTaskDeliveryBinding(
    @Qualifier("c7remote-user-task-delivery")
    remotePullUserTaskDelivery: RemotePullUserTaskDelivery
  ): ScheduledEmbeddedUserTaskDeliveryBinding {
    return ScheduledEmbeddedUserTaskDeliveryBinding(
      userTaskDelivery = remotePullUserTaskDelivery
    )
  }
}
