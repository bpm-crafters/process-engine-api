package dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledUserTaskDeliveryBinding(
  private val pullUserTaskDelivery: PullUserTaskDelivery
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c8.user-tasks.fixed-rate-schedule-rate}")
  fun scheduleUserTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering user tasks..." }
    pullUserTaskDelivery.deliverAll()
    logger.trace { "[SCHEDULER]: Delivered user tasks." }
  }

}
