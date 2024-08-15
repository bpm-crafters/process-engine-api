package dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class SubscribingRefreshingUserTaskDeliveryBinding(
  private val subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c8.user-tasks.schedule-delivery-fixed-rate-in-seconds}")
  fun scheduleUserTaskDelivery() {
    logger.trace { "[SCHEDULER]: Refreshing user tasks..." }
    subscribingRefreshingUserTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Refreshing user tasks." }
  }

  @Scheduled(initialDelayString = "\${dev.bpm-crafters.process-api.adapter.c8.user-tasks.subscribing-delivery-initial-delay-in-seconds}", fixedDelay = Long.MAX_VALUE - 1000)
  fun scheduleTaskSubscription() {
    subscribingRefreshingUserTaskDelivery.subscribe()
  }

}
