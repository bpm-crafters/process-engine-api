package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled

open class ScheduledEmbeddedUserTaskDeliveryBinding(
  private val embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery
) {

  companion object : KLogging()

  @Async
  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7embedded.user-tasks.schedule-delivery-fixed-rate-in-seconds}")
  open fun scheduleUserTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering user tasks..." }
    embeddedPullUserTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Delivered user tasks." }
  }

}
