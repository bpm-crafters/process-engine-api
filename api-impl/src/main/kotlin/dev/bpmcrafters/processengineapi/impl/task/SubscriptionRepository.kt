package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.task.TaskType

/**
 * Repository for managing subscriptions.
 * @since 0.0.1
 */
interface SubscriptionRepository {

  /**
   * Retrieves the list of subscriptions.
   * @return list of subscriptions.
   */
  fun getTaskSubscriptions(): List<TaskSubscriptionHandle>

  /**
   * Creates a new task subscription.
   * @param subscription subscription to create.
   */
  fun createTaskSubscription(subscription: TaskSubscriptionHandle)

  /**
   * Deletes existing task subscription.
   * @param subscription subscription to delete.
   */
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
