package dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class SubscribingServiceTaskDeliveryBinding(
  private val subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery
) {

  companion object : KLogging()

  @Scheduled(
    initialDelayString = "\${dev.bpm-crafters.process-api.adapter.c8.service-tasks.subscribing-delivery-initial-delay-in-seconds}",
    fixedDelay = Long.MAX_VALUE - 1000
  )
  fun scheduleTaskSubscription() {
    subscribingServiceTaskDelivery.subscribe()
  }

}
