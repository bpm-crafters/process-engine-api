package dev.bpmcrafters.processengineapi.adapter.c8.springboot.subscription

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.ServiceTaskDeliveryStrategy.SUBSCRIPTION
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskDeliveryStrategy.SUBSCRIPTION_REFRESHING
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.ConditionalOnServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.ConditionalOnUserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional

@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
@Conditional(C8AdapterEnabledCondition::class)
class C8SubscriptionAutoConfiguration {

  companion object: KLogging()

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C8-203: Subscription configuration applied." }
  }

  @Bean("c8-service-task-delivery-subscription")
  @ConditionalOnServiceTaskDeliveryStrategy(strategy = SUBSCRIPTION)
  fun subscribingServiceTaskDeliveryBinding(
    @Qualifier("c8-service-task-delivery")
    subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery
  ): SubscribingServiceTaskDeliveryBinding {
    return SubscribingServiceTaskDeliveryBinding(
      subscribingServiceTaskDelivery = subscribingServiceTaskDelivery
    )
  }

  @Bean("c8-user-task-delivery-subscription")
  @ConditionalOnUserTaskDeliveryStrategy(strategy = SUBSCRIPTION_REFRESHING)
  fun subscribingUserTaskDeliveryBinding(
    @Qualifier("c8-user-task-delivery")
    subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery,
  ): SubscribingUserTaskDeliveryBinding {
    return SubscribingUserTaskDeliveryBinding(
      subscribingRefreshingUserTaskDelivery = subscribingRefreshingUserTaskDelivery
    )
  }
}
