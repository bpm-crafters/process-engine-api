package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery

/**
 * Delivers user tasks to subscriptions.
 * Uses internal Java API for pulling tasks.
 */
class PullUserTaskDelivery(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskDelivery {

  /**
   * Delivers all tasks found in user task service to corresponding subscriptions.
   */
  fun deliverAll() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    taskService
      .createTaskQuery()
      .forSubscriptions(subscriptions)
      .list()
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

            activeSubscription.action.accept(task.toTaskInformation(), variables)
          }
      }
  }

  private fun TaskQuery.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): TaskQuery {
    // FIXME: narrow down, for the moment take all tasks
    return this
      .active()
  }


  private fun TaskSubscriptionHandle.matches(task: Task): Boolean =
    this.taskType == TaskType.USER && (
      this.taskDescriptionKey == null
        || this.taskDescriptionKey == task.taskDefinitionKey
        || this.taskDescriptionKey == task.id
      )
}

