package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event.EventBasedEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule.ScheduledEmbeddedExternalServiceTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule.ScheduledEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryEnginePlugin
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration for delivery.
 */
@Configuration
@AutoConfigureAfter(C7EmbeddedAdapterAutoConfiguration::class)
class C7EmbeddedDeliveryAutoConfiguration {

  companion object : KLogging()

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_event")
  fun embeddedEventUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskService: TaskService,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): UserTaskDelivery {
    return EmbeddedEventBasedUserTaskDelivery(
      subscriptionRepository = subscriptionRepository
    )
  }

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_event")
  fun configureEventingForUserTaskDelivery(embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery) = EventBasedEmbeddedUserTaskDeliveryBinding(
    embeddedEventBasedUserTaskDelivery
  )

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun externalTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    externalTaskService: ExternalTaskService,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ) = EmbeddedPullExternalTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.externalServiceTasks.workerId,
    maxTasks = c7AdapterProperties.externalServiceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.externalServiceTasks.retryTimeoutInSeconds,
  )

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun embeddedScheduledUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskService: TaskService,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): UserTaskDelivery {
    return EmbeddedPullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskService = taskService
    )
  }

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun embeddedScheduledExternalServiceTaskDeliveryBinding(embeddedPullExternalTaskDelivery: EmbeddedPullExternalTaskDelivery): ScheduledEmbeddedExternalServiceTaskDeliveryBinding {
    return ScheduledEmbeddedExternalServiceTaskDeliveryBinding(
      embeddedPullExternalTaskDelivery = embeddedPullExternalTaskDelivery
    )
  }

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun embeddedScheduledUserTaskDeliveryBinding(embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery): ScheduledEmbeddedUserTaskDeliveryBinding {
    return ScheduledEmbeddedUserTaskDeliveryBinding(
      embeddedPullUserTaskDelivery = embeddedPullUserTaskDelivery
    )
  }

  @Bean
  @ConditionalOnExpression(
    "'\${dev.bpm-crafters.process-api.adapter.c7.embedded.external-service-tasks.delivery-strategy}'.equals('embedded_job')"
      + " or "
      + "'\${dev.bpm-crafters.process-api.adapter.c7.embedded.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedJobTaskDeliveryJobHandler(
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): EmbeddedTaskDeliveryJobHandler {
    return EmbeddedTaskDeliveryJobHandler(
      subscriptionRepository = subscriptionRepository,
      workerId = c7AdapterProperties.externalServiceTasks.workerId,
      lockTimeInSeconds = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds
    )
  }


  @Bean
  @ConditionalOnExpression(
    "'\${dev.bpm-crafters.process-api.adapter.c7.embedded.external-service-tasks.delivery-strategy}'.equals('embedded_job')"
      + " or "
      + "'\${dev.bpm-crafters.process-api.adapter.c7.embedded.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedJobTaskDeliveryBinding(jobHandler: EmbeddedTaskDeliveryJobHandler, c7AdapterProperties: C7EmbeddedAdapterProperties): EmbeddedTaskDeliveryEnginePlugin {
    return EmbeddedTaskDeliveryEnginePlugin(
      jobHandler = jobHandler,
      deliverServiceTasks = c7AdapterProperties.externalServiceTasks.deliveryStrategy == ExternalServiceTaskDeliveryStrategy.EMBEDDED_JOB,
      deliverUserTasks = c7AdapterProperties.userTasks.deliveryStrategy == UserTaskDeliveryStrategy.EMBEDDED_JOB,
    )
  }

}
