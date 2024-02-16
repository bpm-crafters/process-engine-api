package dev.bpmcrafters.processengineapi.adapter.c7.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.c7.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.ExternalTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.externaltask.LockedExternalTask

/**
 * Delivers external tasks to subscriptions.
 */
class ExternalTaskDelivery(
  private val externalTaskService: ExternalTaskService,
  private val workerId: String,
  private val subscriptionRepository: SubscriptionRepository
) {

  /**
   * Delivers all tasks found in the external service to corresponding subscriptions.
   */
  fun deliverAll() {

    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    externalTaskService
      .fetchAndLock(10, workerId)
      .apply {
        subscriptions
          .mapNotNull { it.taskDescriptionKey }
          .distinct()
          .forEach { topic ->
            this.topic(topic, 10) // FIXME -> magic number
          }
      }
      .execute()
      .forEach { lockedTask ->
        subscriptions
          .firstOrNull { subscription -> subscription.matches(lockedTask) }
          ?.let { activeSubscription ->

            subscriptionRepository.activateSubscriptionForTask(lockedTask.id, activeSubscription)

            val variables = if (activeSubscription.payloadDescription.isEmpty()) {
              lockedTask.variables
            } else {
              lockedTask.variables.filter { activeSubscription.payloadDescription.contains(it.key) }
            }

            activeSubscription.action.accept(lockedTask.toTaskInformation(), variables)
          }
      }
  }

  fun deliverOne() {
    // TODO: @jangalinski could you provide your implementation on delivery of a single task, please?
  }

  private fun TaskSubscriptionHandle.matches(task: LockedExternalTask): Boolean {
    return ExternalTaskCompletionStrategy.supports(this.restrictions) &&
      (this.taskDescriptionKey == null || this.taskDescriptionKey == task.topicName)
  }

  private fun LockedExternalTask.toTaskInformation(): TaskInformation {
    return TaskInformation(
      this.id,
      mapOf(
        CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
        CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
        CommonRestrictions.TENANT_ID to this.tenantId,
        CommonRestrictions.TASK_TYPE to "service",
        CommonRestrictions.ACTIVITY_ID to this.activityId
      )
    )
  }
}
