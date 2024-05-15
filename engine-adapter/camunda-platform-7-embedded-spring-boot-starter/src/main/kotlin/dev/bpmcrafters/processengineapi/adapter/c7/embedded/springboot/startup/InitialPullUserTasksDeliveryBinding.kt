package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.startup

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent
import org.springframework.context.event.EventListener


class InitialPullUserTasksDeliveryBinding(
  subscriptionRepository: SubscriptionRepository,
  taskService: TaskService,
) {

  companion object: KLogging()

  private val pullDelivery = EmbeddedPullUserTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    taskService = taskService
  )

  @EventListener
  @Suppress("UNUSED_PARAMETER")
  fun pullUserTasks(event: ProcessApplicationStartedEvent) {
    logger.trace { "[INITIAL PULL]: Delivering user tasks..." }
    pullDelivery.deliverAll()
    logger.trace { "[INITIAL PULL]: Delivered user tasks." }
  }
}
