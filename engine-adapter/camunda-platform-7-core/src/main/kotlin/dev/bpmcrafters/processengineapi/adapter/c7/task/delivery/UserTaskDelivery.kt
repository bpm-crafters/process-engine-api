package dev.bpmcrafters.processengineapi.adapter.c7.task.delivery

import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.UserTaskCompletionStrategy
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task

/**
 * Delivers user tasks to subscriptions.
 */
class UserTaskDelivery(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository
) {

  /**
   * Delivers all tasks found in user task service to corresponding subscriptions.
   */
  fun deliverAll() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    taskService.createTaskQuery().active().list() // FIXME: narrow down, for the moment take all tasks
      .forEach { task ->
        subscriptions
          .firstOrNull { subscription -> subscription.matches(task) }
          ?.let { activeSubscription ->

            subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)

            val variables = if (activeSubscription.payloadDescription.isEmpty()) {
              taskService.getVariables(task.id)
            } else {
              taskService.getVariables(task.id, activeSubscription.payloadDescription)
            }

            activeSubscription.action.accept(task.id, variables)
          }
      }
  }

  private fun TaskSubscriptionHandle.matches(task: Task): Boolean =
    UserTaskCompletionStrategy.supports(this.restrictions) && (
      this.taskDescriptionKey == null || this.taskDescriptionKey == task.taskDefinitionKey || this.taskDescriptionKey == task.id
      )
}
