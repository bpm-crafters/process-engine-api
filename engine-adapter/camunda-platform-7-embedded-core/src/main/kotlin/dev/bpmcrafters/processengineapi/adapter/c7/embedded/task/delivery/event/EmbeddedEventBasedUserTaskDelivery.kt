package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import org.camunda.bpm.engine.delegate.DelegateTask

class EmbeddedEventBasedUserTaskDelivery(
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskDelivery {

  fun userTaskCreated(delegateTask: DelegateTask) {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()

    subscriptions
      .firstOrNull { subscription -> subscription.matches(delegateTask) }
      ?.let { activeSubscription ->

        subscriptionRepository.activateSubscriptionForTask(delegateTask.id, activeSubscription)

        val variables = if (activeSubscription.payloadDescription.isEmpty()) {
          delegateTask.variables
        } else {
          delegateTask.variables.filterKeys { key -> activeSubscription.payloadDescription.contains(key) }
        }

        activeSubscription.action.accept(delegateTask.toTaskInformation(), variables)
      }
  }

  fun userTaskDeleted(delegateTask: DelegateTask) {
    subscriptionRepository.getActiveSubscriptionForTask(delegateTask.id)?.termination?.accept(delegateTask.id)
  }


  private fun TaskSubscriptionHandle.matches(task: DelegateTask): Boolean =
    this.taskType == TaskType.USER && (
      this.taskDescriptionKey == null
        || this.taskDescriptionKey == task.taskDefinitionKey
        || this.taskDescriptionKey == task.id
      )
}
