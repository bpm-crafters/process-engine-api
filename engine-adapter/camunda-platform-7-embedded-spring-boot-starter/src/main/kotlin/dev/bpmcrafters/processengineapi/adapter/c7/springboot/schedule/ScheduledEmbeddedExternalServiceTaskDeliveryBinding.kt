package dev.bpmcrafters.processengineapi.adapter.c7.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.EmbeddedPullExternalTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledEmbeddedExternalServiceTaskDeliveryBinding(
  private val embeddedPullExternalTaskDelivery: EmbeddedPullExternalTaskDelivery,
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7.external-service-tasks.fixed-rate-schedule-rate}")
  fun scheduleExternalTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering external tasks..." }
    embeddedPullExternalTaskDelivery.deliverAll()
    logger.trace { "[SCHEDULER]: Delivered external tasks." }
  }

}
