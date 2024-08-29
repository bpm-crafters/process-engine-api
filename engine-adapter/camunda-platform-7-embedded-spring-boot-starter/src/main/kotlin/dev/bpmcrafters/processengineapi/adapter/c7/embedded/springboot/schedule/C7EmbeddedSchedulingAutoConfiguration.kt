package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.job.C7EmbeddedJobDeliveryAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ExecutorService


@Configuration
@EnableScheduling
@EnableAsync
@AutoConfigureAfter(C7EmbeddedJobDeliveryAutoConfiguration::class)
@ConditionalOnExpression(
  "'\${$DEFAULT_PREFIX.enabled}'.equals('true')"
    + " and ("
    + "'\${$DEFAULT_PREFIX.service-tasks.delivery-strategy}'.equals('embedded_scheduled')"
    + " or "
    + "'\${$DEFAULT_PREFIX.user-tasks.delivery-strategy}'.equals('embedded_scheduled')"
    + ")"
)
class C7EmbeddedSchedulingAutoConfiguration {

  companion object: KLogging()

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-100: applied scheduling configuration" }
  }

  @Bean("c7embedded-task-scheduler")
  @Qualifier("c7embedded-task-scheduler")
  @ConditionalOnMissingBean
  fun taskScheduler(): TaskScheduler {
    val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
    threadPoolTaskScheduler.poolSize = 2 // we have two schedulers, one for user tasks one for service tasks
    threadPoolTaskScheduler.threadNamePrefix = "C7EMBEDDED-SCHEDULER-"
    return threadPoolTaskScheduler
  }

  @Bean("c7embedded-service-task-delivery")
  @Qualifier("c7embedded-service-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.delivery-strategy"], havingValue = "embedded_scheduled")
  fun serviceTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    externalTaskService: ExternalTaskService,
    c7AdapterProperties: C7EmbeddedAdapterProperties,
    @Qualifier("c7embedded-service-task-worker-executor")
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
    repositoryService: RepositoryService,
    c7AdapterProperties: C7EmbeddedAdapterProperties,
    @Qualifier("c7embedded-service-task-worker-executor")
    executorService: ExecutorService
  ): UserTaskDelivery {
    return EmbeddedPullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskService = taskService,
      repositoryService = repositoryService,
      executorService = executorService
    )
  }
}
