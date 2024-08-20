package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7ServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7UserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.subscription.C7TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
@EnableScheduling
@EnableConfigurationProperties(value = [C7EmbeddedAdapterProperties::class])
class C7EmbeddedAdapterAutoConfiguration {

  @Bean("c7embedded-start-process-api")
  @Qualifier("c7embedded-start-process-api")
  fun startProcessApi(runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7embedded-task-subscription-api")
  @Qualifier("c7embedded-task-subscription-api")
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = C7TaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean("c7embedded-correlation-api")
  @Qualifier("c7embedded-correlation-api")
  fun correlationApi(runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7embedded-signal-api")
  @Qualifier("c7embedded-signal-api")
  fun signalApi(runtimeService: RuntimeService): SignalApi = SignalApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7embedded-deployment-api")
  @Qualifier("c7embedded-deployment-api")
  fun deploymentApi(repositoryService: RepositoryService): DeploymentApi = DeploymentApiImpl(
    repositoryService = repositoryService
  )

  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  @Bean("c7embedded-service-task-completion-api")
  @Qualifier("c7embedded-service-task-completion-api")
  fun serviceTaskCompletionApi(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): ServiceTaskCompletionApi =
    C7ServiceTaskCompletionApiImpl(
      workerId = c7AdapterProperties.serviceTasks.workerId,
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository,
      retries = c7AdapterProperties.serviceTasks.retries,
      retryTimeoutInSeconds = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds
    )

  @Bean("c7embedded-user-task-completion-api")
  @Qualifier("c7embedded-user-task-completion-api")
  fun userTaskCompletionApi(
    taskService: TaskService,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi =
    C7UserTaskCompletionApiImpl(
      taskService = taskService,
      subscriptionRepository = subscriptionRepository
    )



}
