package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullServiceTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledRemoteExternalServiceTaskDeliveryBinding(
  private val externalTaskDelivery: RemotePullServiceTaskDelivery,
) {

  companion object : KLogging()

  @Scheduled(fixedRateString = "\${dev.bpm-crafters.process-api.adapter.c7remote.service-tasks.schedule-delivery-fixed-rate-in-seconds}")
  fun scheduleExternalTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering service tasks..." }
    externalTaskDelivery.refresh()
    logger.trace { "[SCHEDULER]: Delivered service tasks." }
  }

}
