package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule.SubscribingRefreshingUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule.ScheduledUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule.SubscribingServiceTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeExternalServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.camunda.zeebe.client.ZeebeClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for task completion.
 */
@Configuration
@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C8ZeebeClientAutoConfiguration {

  @Bean(name = ["c8-service-task-delivery"])
  @Qualifier("c8-service-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "subscription")
  fun subscribingServiceTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    zeebeClient: ZeebeClient,
    c8AdapterProperties: C8AdapterProperties
  ) = SubscribingServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    zeebeClient = zeebeClient,
    workerId = c8AdapterProperties.serviceTasks.workerId
  )

  @Bean("c8-service-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "subscription")
  fun subscribingServiceTaskDeliveryBinding(
    @Qualifier("c8-service-task-delivery")
    subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery
  ): SubscribingServiceTaskDeliveryBinding {
    return SubscribingServiceTaskDeliveryBinding(
      subscribingServiceTaskDelivery = subscribingServiceTaskDelivery
    )
  }

  @Bean(name = ["c8-user-task-delivery"])
  @Qualifier("c8-user-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "subscription_refreshing")
  fun subscribingRefreshingUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    zeebeClient: ZeebeClient,
    c8AdapterProperties: C8AdapterProperties
  ): SubscribingRefreshingUserTaskDelivery {
    return SubscribingRefreshingUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      zeebeClient = zeebeClient,
      workerId = c8AdapterProperties.serviceTasks.workerId,
      userTaskLockTimeoutMs = c8AdapterProperties.userTasks.scheduleDeliveryFixedRateInSeconds
    )
  }

  @Bean("c8-user-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "scheduled")
  fun scheduledUserTaskDeliveryBinding(
    @Qualifier("c8-user-task-delivery")
    pullUserTaskDelivery: PullUserTaskDelivery
  ): ScheduledUserTaskDeliveryBinding {
    return ScheduledUserTaskDeliveryBinding(
      pullUserTaskDelivery = pullUserTaskDelivery
    )
  }

  @Bean("c8-user-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "subscription_refreshing")
  fun refreshingUserTaskDeliveryBinding(
    @Qualifier("c8-user-task-delivery")
    subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery
  ): SubscribingRefreshingUserTaskDeliveryBinding {
    return SubscribingRefreshingUserTaskDeliveryBinding(
      subscribingRefreshingUserTaskDelivery = subscribingRefreshingUserTaskDelivery
    )
  }

  @Bean("c8-service-task-completion")
  @Qualifier("c8-service-task-completion")
  fun externalTaskCompletionStrategy(
    zeebeClient: ZeebeClient,
    subscriptionRepository: SubscriptionRepository,
  ): ServiceTaskCompletionApi =
    C8ZeebeExternalServiceTaskCompletionApiImpl(
      zeebeClient = zeebeClient,
      subscriptionRepository = subscriptionRepository
    )

  @Bean("c8-user-task-completion")
  @Qualifier("c8-user-task-completion")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.completion-strategy"], havingValue = "job")
  fun zeebeUserTaskCompletionStrategy(
    zeebeClient: ZeebeClient,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi =
    C8ZeebeUserTaskCompletionApiImpl(
      zeebeClient = zeebeClient,
      subscriptionRepository = subscriptionRepository
    )

}
