package dev.bpmcrafters.processengineapi.adapter.c8.springboot.subscription

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import mu.KLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

open class SubscribingUserTaskDeliveryBinding(
  private val subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery
) {

  companion object : KLogging()

  @EventListener
  @Async
  open fun scheduleUserTaskSubscription(event: ApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-C8-102: Subscribing to user tasks..." }
    subscribingRefreshingUserTaskDelivery.subscribe()
    logger.trace { "PROCESS-ENGINE-C8-103: Subscribed to user tasks." }
  }

}
