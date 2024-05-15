package dev.bpmcrafters.processengineapi.adapter.c8.task

import dev.bpmcrafters.processengineapi.task.TaskSubscription

/**
 * Common interface for user task delivery.
 */
interface SubscribingUserTaskDelivery {
  fun unsubscribe(taskSubscription: TaskSubscription)
}
