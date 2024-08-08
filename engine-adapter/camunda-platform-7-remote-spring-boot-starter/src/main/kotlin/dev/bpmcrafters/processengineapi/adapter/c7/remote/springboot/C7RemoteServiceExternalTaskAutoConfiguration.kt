package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule.ScheduledEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule.ScheduledRemoteExternalServiceTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteServiceExternalTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteServiceUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Auto-configuration for delivery.
 */
@Configuration
@EnableScheduling
@AutoConfigureAfter(C7RemoteServiceApiAutoConfiguration::class)
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7RemoteServiceExternalTaskAutoConfiguration {

  @Bean("c7remote-service-task-completion-api")
  @Qualifier("c7remote-service-task-completion-api")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun externalTaskCompletionApi(
    @Qualifier("remote") externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7RemoteAdapterProperties
  ): ExternalTaskCompletionApi =
    C7RemoteServiceExternalTaskCompletionApiImpl(
      workerId = c7AdapterProperties.externalServiceTasks.workerId,
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository
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
    c7AdapterProperties: C7RemoteAdapterProperties
  ): UserTaskDelivery {
    return RemotePullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskService = taskService
    )
  }

  @Bean("c7remote-service-task-delivery")
  @Qualifier("c7remote-service-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun scheduledExternalTaskDelivery(
    @Qualifier("remote") externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7RemoteAdapterProperties
  ) = RemotePullExternalTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.externalServiceTasks.workerId,
    maxTasks = c7AdapterProperties.externalServiceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.externalServiceTasks.retryTimeoutInSeconds,
  )

  @Bean("c7remote-service-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_scheduled")
  fun remoteScheduledExternalServiceTaskDeliveryBinding(
    @Qualifier("c7remote-service-task-delivery")
    remotePullExternalTaskDelivery: RemotePullExternalTaskDelivery
  ): ScheduledRemoteExternalServiceTaskDeliveryBinding {
    return ScheduledRemoteExternalServiceTaskDeliveryBinding(
      externalTaskDelivery = remotePullExternalTaskDelivery
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
