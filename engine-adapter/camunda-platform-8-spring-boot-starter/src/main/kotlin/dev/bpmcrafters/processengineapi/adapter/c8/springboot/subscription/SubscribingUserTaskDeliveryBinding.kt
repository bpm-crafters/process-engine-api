package dev.bpmcrafters.processengineapi.adapter.c8.springboot.subscription

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

private val logger = KotlinLogging.logger {}

open class SubscribingUserTaskDeliveryBinding(
  private val subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery
) {

  @EventListener
  @Async
  open fun scheduleUserTaskSubscription(event: ApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-C8-102: Subscribing to user tasks..." }
    subscribingRefreshingUserTaskDelivery.subscribe()
    logger.trace { "PROCESS-ENGINE-C8-103: Subscribed to user tasks." }
  }

}
