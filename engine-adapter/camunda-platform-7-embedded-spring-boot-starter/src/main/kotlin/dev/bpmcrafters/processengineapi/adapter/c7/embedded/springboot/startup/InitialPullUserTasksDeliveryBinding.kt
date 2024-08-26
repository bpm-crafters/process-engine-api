package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.startup

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async


open class InitialPullUserTasksDeliveryBinding(
  subscriptionRepository: SubscriptionRepository,
  taskService: TaskService,
  repositoryService: RepositoryService
) {

  companion object: KLogging()

  private val pullDelivery = EmbeddedPullUserTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    taskService = taskService,
    repositoryService = repositoryService
  )

  @EventListener
  @Suppress("UNUSED_PARAMETER")
  @Async
  open fun pullUserTasks(event: ProcessApplicationStartedEvent) {
    logger.trace { "[INITIAL PULL]: Delivering user tasks..." }
    pullDelivery.refresh()
    logger.trace { "[INITIAL PULL]: Delivered user tasks." }
  }
}
