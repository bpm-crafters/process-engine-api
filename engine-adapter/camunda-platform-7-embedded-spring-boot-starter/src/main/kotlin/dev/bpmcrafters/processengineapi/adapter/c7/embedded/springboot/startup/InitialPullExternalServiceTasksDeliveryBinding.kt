package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.startup

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent
import org.springframework.context.event.EventListener

class InitialPullExternalServiceTasksDeliveryBinding(
  externalTaskService: ExternalTaskService,
  subscriptionRepository: SubscriptionRepository,
  c7AdapterProperties: C7EmbeddedAdapterProperties
) {
  companion object : KLogging()

  private val pullDelivery = EmbeddedPullExternalTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.externalServiceTasks.workerId,
    maxTasks = c7AdapterProperties.externalServiceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.externalServiceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.externalServiceTasks.retryTimeoutInSeconds,
  )

  @EventListener
  @Suppress("UNUSED_PARAMETER")
  fun pullUserTasks(event: ProcessApplicationStartedEvent) {
    logger.trace { "[INITIAL PULL]: Delivering external service tasks..." }
    pullDelivery.deliverAll()
    logger.trace { "[INITIAL PULL]: Delivered external service tasks." }
  }

}
