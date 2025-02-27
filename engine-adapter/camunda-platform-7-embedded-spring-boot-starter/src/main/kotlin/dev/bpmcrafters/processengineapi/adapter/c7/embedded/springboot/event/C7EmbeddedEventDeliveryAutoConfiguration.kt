package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.ConditionalOnUserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

/**
 * Auto-configuration for delivery.
 */
@Configuration
@AutoConfigureAfter(C7EmbeddedAdapterAutoConfiguration::class)
@ConditionalOnUserTaskDeliveryStrategy(
  strategies = [UserTaskDeliveryStrategy.EMBEDDED_EVENT, UserTaskDeliveryStrategy.EMBEDDED_EVENT_AND_SCHEDULED]
)
class C7EmbeddedEventDeliveryAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-204: Configuration applied." }
  }

  @Bean("c7embedded-event-user-task-delivery")
  @Qualifier("c7embedded-event-user-task-delivery")
  fun embeddedEventUserTaskDelivery(
      subscriptionRepository: SubscriptionRepository,
      taskService: TaskService,
      c7AdapterProperties: C7EmbeddedAdapterProperties
  ): UserTaskDelivery {
    return EmbeddedEventBasedUserTaskDelivery(
      subscriptionRepository = subscriptionRepository
    )
  }

  @Bean("c7embedded-event-user-task-delivery-binding")
  fun configureEventingForUserTaskDelivery(
    @Qualifier("c7embedded-event-user-task-delivery")
    embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): C7EmbeddedEmbeddedEventBasedUserTaskUserTaskDeliveryBinding = C7EmbeddedEmbeddedEventBasedUserTaskUserTaskDeliveryBinding(
    embeddedEventBasedUserTaskDelivery = embeddedEventBasedUserTaskDelivery
  )
}
