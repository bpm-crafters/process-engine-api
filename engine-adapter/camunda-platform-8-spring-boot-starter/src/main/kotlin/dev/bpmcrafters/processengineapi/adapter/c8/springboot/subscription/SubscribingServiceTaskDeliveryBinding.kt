package dev.bpmcrafters.processengineapi.adapter.c8.springboot.subscription

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

private val logger = KotlinLogging.logger {}

open class SubscribingServiceTaskDeliveryBinding(
  private val subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery
) {

  @EventListener
  @Async
  open fun scheduleTaskSubscription(event: ApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-C8-100: Subscribing to service tasks..." }
    subscribingServiceTaskDelivery.subscribe()
    logger.trace { "PROCESS-ENGINE-C8-101: Subscribed to service tasks." }
  }

}
