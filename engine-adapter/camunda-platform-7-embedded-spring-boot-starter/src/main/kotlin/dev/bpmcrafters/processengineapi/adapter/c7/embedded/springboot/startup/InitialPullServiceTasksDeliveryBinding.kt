package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.startup

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent
import org.springframework.context.event.EventListener

class InitialPullServiceTasksDeliveryBinding(
  externalTaskService: ExternalTaskService,
  subscriptionRepository: SubscriptionRepository,
  c7AdapterProperties: C7EmbeddedAdapterProperties
) {
  companion object : KLogging()

  private val pullDelivery = EmbeddedPullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.serviceTasks.workerId,
    maxTasks = c7AdapterProperties.serviceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds,
  )

  @EventListener
  @Suppress("UNUSED_PARAMETER")
  fun pullUserTasks(event: ProcessApplicationStartedEvent) {
    logger.trace { "[INITIAL PULL]: Delivering service tasks..." }
    pullDelivery.refresh()
    logger.trace { "[INITIAL PULL]: Delivered service tasks." }
  }

}
