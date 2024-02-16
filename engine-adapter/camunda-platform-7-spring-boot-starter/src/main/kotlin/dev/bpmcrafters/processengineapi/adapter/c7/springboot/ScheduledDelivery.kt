package dev.bpmcrafters.processengineapi.adapter.c7.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.ExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.UserTaskDelivery
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled

class ScheduledDelivery(
  private val externalTaskDelivery: ExternalTaskDelivery,
  private val userTaskDelivery: UserTaskDelivery
) {

  companion object : KLogging()

  @Scheduled(fixedRate = 13_000L)
  fun scheduleExternalTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering external tasks..." }
    externalTaskDelivery.deliverAll()
    logger.trace { "[SCHEDULER]: Delivered external tasks." }
  }

  @Scheduled(fixedRate = 5_000L)
  fun scheduleUserTaskDelivery() {
    logger.trace { "[SCHEDULER]: Delivering user tasks..." }
    userTaskDelivery.deliverAll()
    logger.trace { "[SCHEDULER]: Delivered user tasks." }
  }

}
