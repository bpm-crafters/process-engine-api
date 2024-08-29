package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Configuration
@EnableConfigurationProperties(value = [C7RemoteAdapterProperties::class])
@ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7RemoteAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("c7remote-service-task-worker-executor")
  @Qualifier("c7remote-service-task-worker-executor")
  fun serviceTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

  /**
   * Creates a default fixed thread pool for 10 threads used for process engine worker executions.
   * This one is used for pull-strategies only.
   */
  @Bean("c7remote-user-task-worker-executor")
  @Qualifier("c7remote-user-task-worker-executor")
  fun userTaskWorkerExecutor(): ExecutorService = Executors.newFixedThreadPool(10)

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
