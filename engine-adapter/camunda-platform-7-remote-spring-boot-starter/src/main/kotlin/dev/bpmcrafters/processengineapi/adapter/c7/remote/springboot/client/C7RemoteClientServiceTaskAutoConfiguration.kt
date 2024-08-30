package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.client

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SUBSCRIBED
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteClientServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.FailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.subscribe.SubscribingClientServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.impl.ExternalTaskClientImpl
import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient
import org.camunda.bpm.client.task.ExternalTaskService
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 * Auto-configuration for subscribed delivery.
 */
@Configuration
@AutoConfigureAfter(C7RemoteAdapterAutoConfiguration::class)
@ConditionalOnServiceTaskDeliveryStrategy(
  strategy = REMOTE_SUBSCRIBED
)
@EnableExternalTaskClient
class C7RemoteClientServiceTaskAutoConfiguration {

  companion object : KLogging()

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-201: Configuration applied." }
  }

  @Bean
  fun externalTaskClientExternalTaskService(externalTaskClient: ExternalTaskClient): ExternalTaskService {
    require(externalTaskClient is ExternalTaskClientImpl) { "External task client must be official Camunda External Task Client" }
    return ExternalTaskServiceImpl(externalTaskClient.topicSubscriptionManager.engineClient)
  }

  @Bean("c7remote-service-task-delivery")
  fun subscribingClientExternalTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    externalTaskClient: ExternalTaskClient,
    c7AdapterProperties: C7RemoteAdapterProperties
  ) = SubscribingClientServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    lockDuration = c7AdapterProperties.serviceTasks.lockTimeInSeconds,
    externalTaskClient = externalTaskClient,
    retryTimeout = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = c7AdapterProperties.serviceTasks.retries
  )

  @Bean("c7remote-service-task-completion-api")
  @Qualifier("c7remote-service-task-completion-api")
  fun externalTaskClientCompletionApi(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    @Qualifier("c7remote-failure-retry-supplier")
    failureRetrySupplier: FailureRetrySupplier
  ): ServiceTaskCompletionApi =
    C7RemoteClientServiceTaskCompletionApiImpl(
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository,
      failureRetrySupplier = failureRetrySupplier
    )

}
