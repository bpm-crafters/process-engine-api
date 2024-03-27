package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledEmbeddedUserTaskDeliveryBinding(
  private val embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7.user-tasks.fixed-rate-schedule-rate}")
  fun scheduleUserTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering user tasks..." }
    embeddedPullUserTaskDelivery.deliverAll()
    logger.trace { "[SCHEDULER]: Delivered user tasks." }
  }

}
