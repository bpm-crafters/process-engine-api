package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledEmbeddedServiceTaskDeliveryBinding(
  private val embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery,
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7embedded.service-tasks.schedule-delivery-fixed-rate-in-seconds}")
  fun scheduleExternalTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering external tasks..." }
    embeddedPullServiceTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Delivered external tasks." }
  }

}
