package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.C7RemoteTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteServiceUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.LinearMemoryFailureRetrySupplier
import io.toolisticon.spring.condition.ConditionalOnMissingQualifiedBean
import dev.bpmcrafters.processengineapi.impl.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.community.rest.EnableCamundaRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

@Configuration
@EnableConfigurationProperties(value = [C7RemoteAdapterProperties::class])
@Conditional(C7RemoteAdapterEnabledCondition::class)
@EnableCamundaRestClient
class C7RemoteAdapterAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-200: Configuration applied." }
  }


  @Bean("c7remote-task-subscription-api")
  @Qualifier("c7remote-task-subscription-api")
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = C7RemoteTaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean("c7remote-start-process-api")
  @Qualifier("c7remote-start-process-api")
  fun startProcessApi(@Qualifier("remote") runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7remote-correlation-api")
  @Qualifier("c7remote-correlation-api")
  fun correlationApi(@Qualifier("remote") runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7remote-signal-api")
  @Qualifier("c7remote-signal-api")
  fun signalApi(@Qualifier("remote") runtimeService: RuntimeService): SignalApi = SignalApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7remote-deploy-api")
  @Qualifier("c7remote-deploy-api")
  fun deployApi(@Qualifier("remote") repositoryService: RepositoryService): DeploymentApi = DeploymentApiImpl(
    repositoryService = repositoryService
  )

  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("c7remote-service-task-worker-executor")
  @Qualifier("c7remote-service-task-worker-executor")
  @ConditionalOnMissingQualifiedBean(beanClass = ExecutorService::class, qualifier = "c7remote-service-task-worker-executor")
  fun serviceTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("c7remote-user-task-worker-executor")
  @Qualifier("c7remote-user-task-worker-executor")
  @ConditionalOnMissingQualifiedBean(beanClass = ExecutorService::class, qualifier = "c7remote-user-task-worker-executor")
  fun userTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

  @Bean("c7remote-user-task-completion-api")
  @Qualifier("c7remote-user-task-completion-api")
  fun userTaskCompletionApi(
    @Qualifier("remote") taskService: TaskService,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi =
    C7RemoteServiceUserTaskCompletionApiImpl(
      taskService = taskService,
      subscriptionRepository = subscriptionRepository
    )

  /**
   * Failure retry supplier.
   */
  @Bean("c7remote-failure-retry-supplier")
  @Qualifier("c7remote-failure-retry-supplier")
  @ConditionalOnMissingBean
  fun defaultFailureRetrySupplier(c7AdapterProperties: C7RemoteAdapterProperties): FailureRetrySupplier {
    return LinearMemoryFailureRetrySupplier(
      retry = c7AdapterProperties.serviceTasks.retries,
      retryTimeout = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds
    )
  }

}
