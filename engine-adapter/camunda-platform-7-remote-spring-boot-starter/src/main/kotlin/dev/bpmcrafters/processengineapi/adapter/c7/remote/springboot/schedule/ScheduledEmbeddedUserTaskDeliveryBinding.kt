package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullUserTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledEmbeddedUserTaskDeliveryBinding(
  private val userTaskDelivery: RemotePullUserTaskDelivery
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7remote.user-tasks.fixed-rate-schedule-rate}")
  fun scheduleUserTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering user tasks..." }
    userTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Delivered user tasks." }
  }

}
