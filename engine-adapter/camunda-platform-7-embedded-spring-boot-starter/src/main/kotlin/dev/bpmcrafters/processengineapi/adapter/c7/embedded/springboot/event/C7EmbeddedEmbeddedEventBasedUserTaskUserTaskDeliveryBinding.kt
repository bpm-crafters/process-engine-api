package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

open class C7EmbeddedEmbeddedEventBasedUserTaskUserTaskDeliveryBinding(
  private val embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery,
) {

  @EventListener(condition = "#delegateTask.eventName.equals('create')")
  @Order(Ordered.HIGHEST_PRECEDENCE + 1000)
  open fun onTaskCreate(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskCreated(delegateTask = delegateTask)
  }

  @EventListener(condition = "#delegateTask.eventName.equals('delete') || #delegateTask.eventName.equals('timeout')")
  @Order(Ordered.HIGHEST_PRECEDENCE + 1000)
  open fun onTaskDelete(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskDeleted(delegateTask = delegateTask)
  }

}
