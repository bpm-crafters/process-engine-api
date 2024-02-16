package dev.bpmcrafters.processengineapi.adapter.c7.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.task.TaskInformation
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

            activeSubscription.action.accept(task.toInformation(), variables)
          }
      }
  }

  private fun TaskSubscriptionHandle.matches(task: Task): Boolean =
    UserTaskCompletionStrategy.supports(this.restrictions) && (
      this.taskDescriptionKey == null || this.taskDescriptionKey == task.taskDefinitionKey || this.taskDescriptionKey == task.id
      )

  private fun Task.toInformation() =
    TaskInformation(
      taskId = this.id,
      meta = mapOf(
        CommonRestrictions.TASK_TYPE to "user",
        CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionKey,
        CommonRestrictions.TENANT_ID to this.tenantId,
        CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
        "taskName" to this.name,
        "taskDescription" to this.description,
        "assignee" to this.assignee
      )
    )
}
