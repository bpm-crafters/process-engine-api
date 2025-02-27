package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterServiceTaskInitialPullEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterUserTaskInitialPullEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event.C7EmbeddedEventDeliveryAutoConfiguration
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
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
@AutoConfigureAfter(C7EmbeddedEventDeliveryAutoConfiguration::class)
@EnableAsync
class C7EmbeddedInitialPullOnStartupAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-203: Configuration for initial pull applied." }
  }

  @Bean("c7embedded-user-task-initial-pull")
  @Qualifier("c7embedded-user-task-initial-pull")
  @Conditional(C7EmbeddedAdapterUserTaskInitialPullEnabledCondition::class)
  fun configureInitialPullForUserTaskDelivery(
      taskService: TaskService,
      repositoryService: RepositoryService,
      subscriptionRepository: SubscriptionRepository,
      @Qualifier("c7embedded-user-task-worker-executor")
    executorService: ExecutorService
  ) = C7EmbeddedInitialPullUserTasksDeliveryBinding(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository,
    repositoryService = repositoryService,
    executorService = executorService
  )

  @Bean("c7embedded-service-task-initial-pull")
  @Qualifier("c7embedded-service-task-initial-pull")
  @Conditional(C7EmbeddedAdapterServiceTaskInitialPullEnabledCondition::class)
  fun configureInitialPullForExternalServiceTaskDelivery(
      externalTaskService: ExternalTaskService,
      subscriptionRepository: SubscriptionRepository,
      c7AdapterProperties: C7EmbeddedAdapterProperties,
      @Qualifier("c7embedded-service-task-worker-executor")
    executorService: ExecutorService
  ) = C7EmbeddedInitialPullServiceTasksDeliveryBinding(
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository,
    c7AdapterProperties = c7AdapterProperties,
    executorService = executorService
  )
}
