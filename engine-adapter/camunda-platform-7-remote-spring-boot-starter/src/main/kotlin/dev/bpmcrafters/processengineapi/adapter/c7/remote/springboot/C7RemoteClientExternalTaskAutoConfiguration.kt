package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.completion.C7RemoteClientExternalTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.subscribe.SubscribingClientExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.impl.ExternalTaskClientImpl
import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient
import org.camunda.bpm.client.task.ExternalTaskService
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean


@AutoConfigureAfter(C7RemoteAdapterAutoConfiguration::class)
@ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_subscribed")
@EnableExternalTaskClient
class C7RemoteClientExternalTaskAutoConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_subscribed")
  fun externalTaskClientExternalTaskService(externalTaskClient: ExternalTaskClient): ExternalTaskService {
    require(externalTaskClient is ExternalTaskClientImpl) { "External task client must be official Camunda External Task Client" }
    return ExternalTaskServiceImpl(externalTaskClient.topicSubscriptionManager.engineClient)
  }

  @Bean
  @ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_subscribed")
  fun externalTaskClientCompletionApi(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7RemoteAdapterProperties
  ): ExternalTaskCompletionApi =
    C7RemoteClientExternalTaskCompletionApiImpl(
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository
    )

  @Bean
  @ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["external-service-tasks.delivery-strategy"], havingValue = "remote_subscribed")
  fun subscribingClientExternalTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    externalTaskClient: ExternalTaskClient,
    c7AdapterProperties: C7RemoteAdapterProperties
  ) = SubscribingClientExternalTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    lockDuration = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds,
    externalTaskClient = externalTaskClient
  )

}
