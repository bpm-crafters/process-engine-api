package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.C7RemoteTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableConfigurationProperties(value = [C7RemoteAdapterProperties::class])
class C7RemoteAdapterAutoConfiguration {

  @Bean
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = C7RemoteTaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()
}
