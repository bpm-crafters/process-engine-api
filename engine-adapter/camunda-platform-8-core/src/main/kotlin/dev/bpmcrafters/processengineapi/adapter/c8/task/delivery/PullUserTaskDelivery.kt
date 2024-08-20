package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.tasklist.dto.Task
import io.camunda.tasklist.dto.TaskSearch
import io.camunda.tasklist.dto.TaskState

class PullUserTaskDelivery(
  private val taskListClient: CamundaTaskListClient,
  private val subscriptionRepository: SubscriptionRepository
) : RefreshableDelivery {

  override fun refresh() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()

    // FIXME -> reverse lookup for all active subscriptions
    // if the task is not retrieved but active subscription has a task, call modification#terminated hook

    taskListClient.getTasks(
      TaskSearch()
        .forSubscriptions(subscriptions)
        .setWithVariables(true)
        .setState(TaskState.CREATED) // deliver only open tasks
    ).forEach { task ->
      subscriptions
        .firstOrNull { subscription -> subscription.matches(task) }
        ?.let { activeSubscription ->

          subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)

          val variables : Map<String, Any> = if (activeSubscription.payloadDescription == null) {
            task.variables
          } else {
            if (activeSubscription.payloadDescription!!.isEmpty()) {
              task.variables?.filter { activeSubscription.payloadDescription!!.contains(it.name) }
            } else {
              listOf()
            }
          }?.associate { variable ->
            variable.name to variable.value
          } ?: mapOf()

          try {
            activeSubscription.action.accept(task.toTaskInformation(), variables)
          } catch (e: Exception) {
            subscriptionRepository.deactivateSubscriptionForTask(taskId = task.id)
          }
        }
    }
  }

  private fun TaskSearch.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): TaskSearch {
    subscriptions
      .filter { it.taskType == TaskType.USER } // only user task subscriptions
      .map { it.taskDescriptionKey to it.restrictions }

    return this
  }

  private fun TaskSubscriptionHandle.matches(task: Task): Boolean =
    this.taskType == TaskType.USER && (this.taskDescriptionKey == null || this.taskDescriptionKey == task.taskDefinitionId)
}
