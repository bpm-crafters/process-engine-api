package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
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
    subscriptionRepository.getActiveSubscriptionForTask(delegateTask.id)?.modification?.terminated(delegateTask.id)
  }


  private fun TaskSubscriptionHandle.matches(task: DelegateTask): Boolean =
    UserTaskCompletionStrategy.supports(this.restrictions)
      && (this.taskDescriptionKey == null || this.taskDescriptionKey == task.taskDefinitionKey || this.taskDescriptionKey == task.id)

}
