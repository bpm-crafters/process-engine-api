package dev.bpmcrafters.processengineapi.adapter.c8.springboot.subscription

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import mu.KLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

open class SubscribingServiceTaskDeliveryBinding(
  private val subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery
) {

  companion object : KLogging()

  @EventListener
  @Async
  open fun scheduleTaskSubscription(event: ApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-C8-100: Subscribing to service tasks..." }
    subscribingServiceTaskDelivery.subscribe()
    logger.trace { "PROCESS-ENGINE-C8-101: Subscribed to service tasks." }
  }

}
