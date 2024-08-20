package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.filterBySubscription
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery.Companion.logger
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

        val variables = delegateTask.variables.filterBySubscription(activeSubscription)

        try {
          activeSubscription.action.accept(delegateTask.toTaskInformation(), variables)
        } catch (e: Exception) {
          logger.error { "[PROCESS-ENGINE-C7-EMBEDDED] Error delivering task ${delegateTask.id}: ${e.message}" }
          subscriptionRepository.deactivateSubscriptionForTask(taskId = delegateTask.id)
        }
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
