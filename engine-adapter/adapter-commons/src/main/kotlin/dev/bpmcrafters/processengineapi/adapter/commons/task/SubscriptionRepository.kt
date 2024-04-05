package dev.bpmcrafters.processengineapi.adapter.commons.task

import dev.bpmcrafters.processengineapi.task.TaskType

/**
 * Repository for managing subscriptions.
 * @since 0.0.1
 */
interface SubscriptionRepository {

  fun getTaskSubscriptions(): List<TaskSubscriptionHandle>
  fun createTaskSubscription(subscription: TaskSubscriptionHandle)
  fun deleteTaskSubscription(subscription: TaskSubscriptionHandle)

  /**
   * Activate a subscription delivering a task to it.
   * @param taskId id of the task.
   * @param subscription subscription that received a task.
   */
  fun activateSubscriptionForTask(taskId: String, subscription: TaskSubscriptionHandle)

  /**
   * Retrieves a subscription for task.
   * @param taskId id of the task.
   * @return subscription handle or null.
   */
  fun getActiveSubscriptionForTask(taskId: String): TaskSubscriptionHandle?

  /**
   * Deactivates subscription for task.
   * @param taskId task id.
   * @return task subscription, if it was present.
   */
  fun deactivateSubscriptionForTask(taskId: String): TaskSubscriptionHandle?

  /**
   * Retrieves a list of delivered tasks.
   * @param taskType type of the subscribed task.
   * @return list of task ids.
   */
  fun getDeliveredTaskIds(taskType: TaskType): List<String>
}
