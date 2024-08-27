package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.initial

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.initial.InitialPullServiceTasksDeliveryBinding.Companion.ORDER
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.ExecutorService

/**
 * This class is responsible for the initial pull of user tasks.
 * We are not relying on the pull delivery strategy configured centrally, because for other deliveries we still want to
 * execute an initial pull (e.g. for event-based delivery)
 */
@Order(ORDER)
open class InitialPullServiceTasksDeliveryBinding(
  externalTaskService: ExternalTaskService,
  subscriptionRepository: SubscriptionRepository,
  c7AdapterProperties: C7EmbeddedAdapterProperties,
  executorService: ExecutorService
) {
  companion object: KLogging() {
    const val ORDER = Ordered.HIGHEST_PRECEDENCE + 1000
  }


  private val pullDelivery = EmbeddedPullServiceTaskDelivery(
    subscriptionRepository = subscriptionRepository,
    externalTaskService = externalTaskService,
    workerId = c7AdapterProperties.serviceTasks.workerId,
    maxTasks = c7AdapterProperties.serviceTasks.maxTaskCount,
    lockDuration = c7AdapterProperties.serviceTasks.lockTimeInSeconds,
    retryTimeout = c7AdapterProperties.serviceTasks.retryTimeoutInSeconds,
    retries = c7AdapterProperties.serviceTasks.retries,
    executorService = executorService
  )

  @EventListener
  @Async
  open fun pullUserTasks(event: ProcessApplicationStartedEvent) {
    logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-101: Delivering service tasks..." }
    pullDelivery.refresh()
    logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-102: Delivered service tasks." }
  }

}
