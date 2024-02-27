package dev.bpmcrafters.processengineapi.adapter.c7.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.springboot.C7AdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.springboot.C7AdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.springboot.C7AdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.springboot.event.EventBasedEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.springboot.schedule.ScheduledEmbeddedExternalServiceTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.springboot.schedule.ScheduledEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.EmbeddedEventBasedUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.EmbeddedPullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.job.EmbeddedTaskDeliveryEnginePlugin
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.job.EmbeddedTaskDeliveryJobHandler
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
@AutoConfigureAfter(AdapterAutoConfiguration::class)
class DeliveryAutoConfiguration {

  companion object : KLogging()

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_event")
  fun embeddedEventUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskService: TaskService,
    c7AdapterProperties: C7AdapterProperties
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
    c7AdapterProperties: C7AdapterProperties
  ) = EmbeddedPullExternalTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.externalServiceTasks.workerId,
    maxTasks = c7AdapterProperties.externalServiceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds
  )

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun embeddedScheduledUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskService: TaskService,
    c7AdapterProperties: C7AdapterProperties
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
    "'\${dev.bpm-crafters.process-api.adapter.c7.external-service-tasks.delivery-strategy}'.equals('embedded_job') or '\${dev.bpm-crafters.process-api.adapter.c7.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedJobTaskDeliveryJobHandler(
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7AdapterProperties
  ): EmbeddedTaskDeliveryJobHandler {
    return EmbeddedTaskDeliveryJobHandler(
      subscriptionRepository = subscriptionRepository,
      workerId = c7AdapterProperties.externalServiceTasks.workerId,
      lockTimeInSecconds = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds
    )
  }


  @Bean
  @ConditionalOnExpression(
    "'\${dev.bpm-crafters.process-api.adapter.c7.external-service-tasks.delivery-strategy}'.equals('embedded_job') or '\${dev.bpm-crafters.process-api.adapter.c7.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedJobTaskDeliveryBinding(jobHandler: EmbeddedTaskDeliveryJobHandler, c7AdapterProperties: C7AdapterProperties): EmbeddedTaskDeliveryEnginePlugin {
    return EmbeddedTaskDeliveryEnginePlugin(
      jobHandler = jobHandler,
      deliverServiceTasks = c7AdapterProperties.externalServiceTasks.deliveryStrategy == ExternalServiceTaskDeliveryStrategy.EMBEDDED_JOB,
      deliverUserTasks = c7AdapterProperties.userTasks.deliveryStrategy == UserTaskDeliveryStrategy.EMBEDDED_JOB,
    )
  }

}
