package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.client.C7RemoteClientServiceTaskAutoConfiguration
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * This configuration configures the initial pull bound to the application started event.
 * It is not relying on any delivery strategies but just configures the initial pull to happen
 * and deliver tasks to the task handlers.
 */
@Configuration
@AutoConfigureAfter(C7RemoteClientServiceTaskAutoConfiguration::class)
@EnableAsync
@Conditional(C7RemoteAdapterEnabledCondition::class)
class C7RemoteInitialPullOnStartupAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-203: Configuration applied." }
  }

  @Bean("c7remote-user-task-initial-pull")
  @Qualifier("c7remote-user-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForUserTaskDelivery(
      taskService: TaskService,
      repositoryService: RepositoryService,
      subscriptionRepository: SubscriptionRepository,
      @Qualifier("c7remote-user-task-worker-executor")
    executorService: ExecutorService
  ) = C7RemoteInitialPullUserTasksDeliveryBinding(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository,
    repositoryService = repositoryService,
    executorService = executorService
  )

  @Bean("c7remote-service-task-initial-pull")
  @Qualifier("c7remote-service-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForExternalServiceTaskDelivery(
      externalTaskService: ExternalTaskService,
      subscriptionRepository: SubscriptionRepository,
      c7AdapterProperties: C7RemoteAdapterProperties,
      @Qualifier("c7remote-service-task-worker-executor")
    executorService: ExecutorService
  ) = C7RemoteInitialPullServiceTasksDeliveryBinding(
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository,
    c7AdapterProperties = c7AdapterProperties,
    executorService = executorService
  )
}
