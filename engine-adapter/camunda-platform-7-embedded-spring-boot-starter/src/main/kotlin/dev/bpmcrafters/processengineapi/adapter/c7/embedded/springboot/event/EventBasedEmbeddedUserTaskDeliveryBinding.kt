package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.EmbeddedEventBasedUserTaskDelivery
import org.camunda.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

class EventBasedEmbeddedUserTaskDeliveryBinding(
  private val embeddedEventBasedUserTaskDelivery: EmbeddedEventBasedUserTaskDelivery
) {

  @EventListener(
    condition = "#delegateTask.eventName.equals('create')"
  )
  fun onTaskCreate(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskCreated(delegateTask = delegateTask)
  }

  @EventListener(
    condition = "#delegateTask.eventName.equals('update') || #delegateTask.eventName.equals('assignment')"
  )
  fun onTaskUpdate(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskModified(delegateTask = delegateTask)
  }

  @EventListener(
    condition = "#delegateTask.eventName.equals('delete') || #delegateTask.eventName.equals('timout')"
  )
  fun onTaskDelete(delegateTask: DelegateTask) {
    embeddedEventBasedUserTaskDelivery.userTaskDeleted(delegateTask = delegateTask)
  }
}
