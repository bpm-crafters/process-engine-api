package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullExternalTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledRemoteExternalServiceTaskDeliveryBinding(
  private val externalTaskDelivery: RemotePullExternalTaskDelivery,
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7remote.external-service-tasks.fixed-rate-schedule-rate}")
  fun scheduleExternalTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering external tasks..." }
    externalTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Delivered external tasks." }
  }

}
