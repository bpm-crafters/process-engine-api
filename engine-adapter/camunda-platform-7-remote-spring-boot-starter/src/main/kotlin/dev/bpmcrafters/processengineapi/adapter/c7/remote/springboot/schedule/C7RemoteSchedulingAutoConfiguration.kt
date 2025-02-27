package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.*
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteServiceServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullUserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * Auto-configuration for scheduled delivery.
 */
@Configuration
@EnableScheduling
@EnableAsync
@AutoConfigureAfter(C7RemoteAdapterAutoConfiguration::class)
@Conditional(C7RemoteAdapterEnabledCondition::class)
class C7RemoteSchedulingAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-202: Configuration applied." }
  }

  @Bean("c7remote-task-scheduler")
  @Qualifier("c7remote-task-scheduler")
  @ConditionalOnMissingBean
  fun taskScheduler(): TaskScheduler {
    val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
    threadPoolTaskScheduler.poolSize = 2 // we have two schedulers, one for user tasks one for service tasks
    threadPoolTaskScheduler.threadNamePrefix = "C7REMOTE-SCHEDULER-"
    return threadPoolTaskScheduler
  }

  @Bean("c7remote-service-task-delivery")
  @Qualifier("c7remote-service-task-delivery")
  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun scheduledServiceTaskDelivery(
      @Qualifier("remote") externalTaskService: ExternalTaskService,
      subscriptionRepository: SubscriptionRepository,
      c7AdapterProperties: C7RemoteAdapterProperties,
      @Qualifier("c7remote-service-task-worker-executor") executorService: ExecutorService,
  ) = RemotePullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.serviceTasks.workerId,
    maxTasks = c7AdapterProperties.serviceTasks.maxTaskCount,
    lockDurationInSeconds = c7AdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeoutInSeconds = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = c7AdapterProperties.serviceTasks.retries,
    executorService = executorService
  )

  @Bean("c7remote-service-task-completion-api")
  @Qualifier("c7remote-service-task-completion-api")
  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
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

  @Bean("c7remote-user-task-delivery")
  @Qualifier("c7remote-user-task-delivery")
  @ConditionalOnUserTaskDeliveryStrategy(
    strategy = UserTaskDeliveryStrategy.REMOTE_SCHEDULED
  )
  fun userTaskDelivery(
      @Qualifier("remote") taskService: TaskService,
      subscriptionRepository: SubscriptionRepository,
      c7AdapterProperties: C7RemoteAdapterProperties,
      @Qualifier("remote") repositoryService: RepositoryService,
      @Qualifier("c7remote-user-task-worker-executor") executorService: ExecutorService,
  ): RemotePullUserTaskDelivery {
    return RemotePullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskService = taskService,
      repositoryService = repositoryService,
      executorService = executorService
    )
  }

}
