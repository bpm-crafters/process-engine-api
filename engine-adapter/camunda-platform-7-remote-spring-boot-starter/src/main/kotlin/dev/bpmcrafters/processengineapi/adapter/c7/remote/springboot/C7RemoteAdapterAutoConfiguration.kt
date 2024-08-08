package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.C7RemoteTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableConfigurationProperties(value = [C7RemoteAdapterProperties::class])
@ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7RemoteAdapterAutoConfiguration {

  @Bean("c7remote-task-subscription-api")
  @Qualifier("c7remote-task-subscription-api")
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = C7RemoteTaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()
}
