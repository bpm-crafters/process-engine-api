package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule.ScheduledUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.client.ZeebeClient
import mu.KLogging
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration for delivery.
 */
@Configuration
@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
class DeliveryAutoConfiguration {

  companion object : KLogging()

  @Bean(initMethod = "subscribe")
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

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "scheduled")
  fun scheduledUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskListClient: CamundaTaskListClient,
  ): PullUserTaskDelivery {
    return PullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskListClient = taskListClient
    )
  }

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "scheduled")
  fun scheduledUserTaskDeliveryBinding(pullUserTaskDelivery: PullUserTaskDelivery): ScheduledUserTaskDeliveryBinding {
    return ScheduledUserTaskDeliveryBinding(
      pullUserTaskDelivery = pullUserTaskDelivery
    )
  }
}
