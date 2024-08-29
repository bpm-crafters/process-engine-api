package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration for delivery.
 */
@Configuration
@AutoConfigureAfter(C7EmbeddedAdapterAutoConfiguration::class)
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7EmbeddedEventDeliveryAutoConfiguration {


  companion object : KLogging()

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-204: Configuration applied." }
  }

  @Bean("c7embedded-user-task-delivery")
  @Qualifier("c7embedded-user-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_event")
  fun embeddedEventUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskService: TaskService,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): UserTaskDelivery {
    return EmbeddedEventBasedUserTaskDelivery(
      subscriptionRepository = subscriptionRepository
    )
  }

  @Bean("c7embedded-user-task-delivery-binding")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "embedded_event")
  fun configureEventingForUserTaskDelivery(
    @Qualifier("c7embedded-user-task-delivery")
    embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery
  ) = C7EmbeddedEventBasedUserTaskDeliveryBinding(
    embeddedEventBasedUserTaskDelivery = embeddedEventBasedUserTaskDelivery
  )
}
