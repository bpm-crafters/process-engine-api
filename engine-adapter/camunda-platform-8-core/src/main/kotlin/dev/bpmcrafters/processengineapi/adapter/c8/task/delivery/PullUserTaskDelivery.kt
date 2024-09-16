package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.tasklist.dto.Task
import io.camunda.tasklist.dto.TaskSearch
import io.camunda.tasklist.dto.TaskState
import mu.KLogging

class PullUserTaskDelivery(
  private val taskListClient: CamundaTaskListClient,
  private val subscriptionRepository: SubscriptionRepository
) : RefreshableDelivery {

  companion object : KLogging()

  override fun refresh() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()

    // FIXME -> reverse lookup for all active subscriptions
    // if the task is not retrieved but active subscription has a task, call modification#terminated hook
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C8-030: pulling user tasks for subscriptions: $subscriptions" }
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

            val variablesFromTask: Map<String, Any> = task.variables?.associate { variable ->
              variable.name to variable.value
            } ?: mapOf()

            val variables = variablesFromTask.filterBySubscription(activeSubscription)

            try {
              logger.debug { "PROCESS-ENGINE-C8-031: delivering user task ${task.id}." }
              activeSubscription.action.accept(task.toTaskInformation(), variables)
              logger.debug { "PROCESS-ENGINE-C8-032: successfully delivered user task ${task.id}." }
            } catch (e: Exception) {
              logger.error { "PROCESS-ENGINE-C8-031: error delivering user task ${task.id}: ${e.message}" }
              subscriptionRepository.deactivateSubscriptionForTask(taskId = task.id)
            }
          }
      }
    } else {
      logger.trace { "PROCESS-ENGINE-C8-035: pulling user tasks disabled, no subscriptions." }
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
