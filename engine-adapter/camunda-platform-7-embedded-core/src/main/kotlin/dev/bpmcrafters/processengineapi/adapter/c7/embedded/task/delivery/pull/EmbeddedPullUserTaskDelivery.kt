package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.filterBySubscription
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import mu.KLogging
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery

/**
 * Delivers user tasks to subscriptions.
 * Uses internal Java API for pulling tasks.
 */
class EmbeddedPullUserTaskDelivery(
  private val taskService: TaskService,
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskDelivery, RefreshableDelivery {

  companion object: KLogging()

  /**
   * Delivers all tasks found in user task service to corresponding subscriptions.
   */
  override fun refresh() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if(subscriptions.isNotEmpty()) {
      logger.trace { "Pull user tasks for subscriptions: $subscriptions" }
      taskService
        .createTaskQuery()
        .forSubscriptions(subscriptions)
        .list()
        .forEach { task ->
          subscriptions
            .firstOrNull { subscription -> subscription.matches(task) }
            ?.let { activeSubscription ->

              subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)

              val variables = taskService.getVariables(task.id).filterBySubscription(activeSubscription)

              try {
                activeSubscription.action.accept(task.toTaskInformation(), variables)
              } catch (e: Exception) {
                logger.error { "[PROCESS-ENGINE-C7-EMBEDDED]: Error delivering task ${task.id}: ${e.message}" }
                subscriptionRepository.deactivateSubscriptionForTask(taskId = task.id)
              }
            }
        }
      } else {
      logger.trace { "Pull user tasks disabled because of no active subscriptions" }
    }
  }

  @Suppress("UNUSED_PARAMETER")
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

