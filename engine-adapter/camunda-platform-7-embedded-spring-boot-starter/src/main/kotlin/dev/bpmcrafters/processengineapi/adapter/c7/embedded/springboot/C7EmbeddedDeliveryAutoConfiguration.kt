package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event.EventBasedEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule.ScheduledEmbeddedServiceTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule.ScheduledEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryEnginePlugin
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Auto-configuration for delivery.
 */
@Configuration
@AutoConfigureAfter(C7EmbeddedAdapterAutoConfiguration::class)
class C7EmbeddedDeliveryAutoConfiguration {

  companion object : KLogging()

  @Bean("c7embedded-user-task-delivery")
  @Qualifier("c7embedded-user-task-delivery")
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

  @Bean("c7embedded-user-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_event")
  fun configureEventingForUserTaskDelivery(
    @Qualifier("c7embedded-user-task-delivery")
    embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery
  ) = EventBasedEmbeddedUserTaskDeliveryBinding(
    embeddedEventBasedUserTaskDelivery
  )

  @Bean("c7embedded-service-task-delivery")
  @Qualifier("c7embedded-service-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun serviceTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    externalTaskService: ExternalTaskService,
    c7AdapterProperties: C7EmbeddedAdapterProperties,
    @Qualifier("processEngineWorkerTaskExecutor")
    executorService: ExecutorService
  ) = EmbeddedPullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.serviceTasks.workerId,
    maxTasks = c7AdapterProperties.serviceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = c7AdapterProperties.serviceTasks.retries,
    executorService = executorService
  )

  @Bean("c7embedded-user-task-delivery")
  @Qualifier("c7embedded-user-task-delivery")
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

  @Bean("c7embedded-service-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun embeddedScheduledExternalServiceTaskDeliveryBinding(
    @Qualifier("c7embedded-service-task-delivery")
    embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery): ScheduledEmbeddedServiceTaskDeliveryBinding {
    return ScheduledEmbeddedServiceTaskDeliveryBinding(
      embeddedPullServiceTaskDelivery = embeddedPullServiceTaskDelivery
    )
  }

  @Bean("c7embedded-user-task-delivery-scheduler")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun embeddedScheduledUserTaskDeliveryBinding(
    @Qualifier("c7embedded-user-task-delivery")
    embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery
  ): ScheduledEmbeddedUserTaskDeliveryBinding {
    return ScheduledEmbeddedUserTaskDeliveryBinding(
      embeddedPullUserTaskDelivery = embeddedPullUserTaskDelivery
    )
  }

  @Bean("c7embedded-task-delivery-job-handler")
  @ConditionalOnExpression(
    "'\${$DEFAULT_PREFIX.service-tasks.delivery-strategy}'.equals('embedded_job')"
      + " or "
      + "'\${$DEFAULT_PREFIX.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedTaskDeliveryJobHandler(
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): EmbeddedTaskDeliveryJobHandler {
    return EmbeddedTaskDeliveryJobHandler(
      subscriptionRepository = subscriptionRepository,
      workerId = c7AdapterProperties.serviceTasks.workerId,
      lockTimeInSeconds = c7AdapterProperties.serviceTasks.lockTimeInSeconds
    )
  }


  @Bean("c7embedded-task-delivery-engine-plugin")
  @ConditionalOnExpression(
    "'\${$DEFAULT_PREFIX.service-tasks.delivery-strategy}'.equals('embedded_job')"
      + " or "
      + "'\${$DEFAULT_PREFIX.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedTaskDeliveryBinding(
    jobHandler: EmbeddedTaskDeliveryJobHandler,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): EmbeddedTaskDeliveryEnginePlugin {
    return EmbeddedTaskDeliveryEnginePlugin(
      jobHandler = jobHandler,
      deliverServiceTasks = c7AdapterProperties.serviceTasks.deliveryStrategy == ExternalServiceTaskDeliveryStrategy.EMBEDDED_JOB,
      deliverUserTasks = c7AdapterProperties.userTasks.deliveryStrategy == UserTaskDeliveryStrategy.EMBEDDED_JOB,
    )
  }
}
