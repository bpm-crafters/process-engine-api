package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.startup.InitialPullServiceTasksDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.startup.InitialPullUserTasksDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService

@Configuration
@AutoConfigureAfter(C7EmbeddedDeliveryAutoConfiguration::class)
class C7EmbeddedInitialPullOnStartupAutoConfiguration {


  @Bean("c7embedded-user-task-initial-pull")
  @Qualifier("c7embedded-user-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForUserTaskDelivery(
    taskService: TaskService,
    subscriptionRepository: SubscriptionRepository
  ) = InitialPullUserTasksDeliveryBinding(
    taskService = taskService,
    subscriptionRepository = subscriptionRepository
  )

  @Bean("c7embedded-service-task-initial-pull")
  @Qualifier("c7embedded-service-task-initial-pull")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["service-tasks.execute-initial-pull-on-startup"])
  fun configureInitialPullForExternalServiceTaskDelivery(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7EmbeddedAdapterProperties,
    @Qualifier("processEngineWorkerTaskExecutor")
    executorService: ExecutorService
  ) = InitialPullServiceTasksDeliveryBinding(
    externalTaskService = externalTaskService,
    subscriptionRepository = subscriptionRepository,
    c7AdapterProperties = c7AdapterProperties,
    executorService = executorService
  )
}
