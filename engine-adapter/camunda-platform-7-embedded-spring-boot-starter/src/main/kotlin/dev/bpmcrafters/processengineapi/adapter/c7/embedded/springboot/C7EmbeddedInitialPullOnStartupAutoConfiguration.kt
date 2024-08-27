package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.initial.InitialPullServiceTasksDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.initial.InitialPullUserTasksDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.ExecutorService

/**
 * This configuration configures the initial pull bound to the application started event.
 * It is not relying on any delivery strategies but just configures the initial pull to happen
 * and deliver tasks to the task handlers.
 */
@Configuration
@AutoConfigureAfter(C7EmbeddedJobDeliveryAutoConfiguration::class)
@EnableAsync
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7EmbeddedInitialPullOnStartupAutoConfiguration {

  @Bean("c7embedded-user-task-initial-pull")
  @Qualifier("c7embedded-user-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForUserTaskDelivery(
    taskService: TaskService,
    repositoryService: RepositoryService,
    subscriptionRepository: SubscriptionRepository,
    @Qualifier("c7embedded-user-task-worker-executor")
    executorService: ExecutorService
  ) = InitialPullUserTasksDeliveryBinding(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository,
    repositoryService = repositoryService,
    executorService = executorService
  )

  @Bean("c7embedded-service-task-initial-pull")
  @Qualifier("c7embedded-service-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForExternalServiceTaskDelivery(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7EmbeddedAdapterProperties,
    @Qualifier("c7embedded-service-task-worker-executor")
    executorService: ExecutorService
  ) = InitialPullServiceTasksDeliveryBinding(
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository,
    c7AdapterProperties = c7AdapterProperties,
    executorService = executorService
  )
}
