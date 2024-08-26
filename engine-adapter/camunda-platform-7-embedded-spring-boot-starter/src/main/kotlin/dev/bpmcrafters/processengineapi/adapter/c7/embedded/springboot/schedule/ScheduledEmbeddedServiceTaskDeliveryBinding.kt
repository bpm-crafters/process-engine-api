package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled

open class ScheduledEmbeddedServiceTaskDeliveryBinding(
  private val embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery,
) {

  companion object : KLogging()

  @Async
  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7embedded.service-tasks.schedule-delivery-fixed-rate-in-seconds}")
  open fun scheduleExternalTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering external tasks..." }
    embeddedPullServiceTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Delivered external tasks." }
  }

}
