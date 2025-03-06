package dev.bpmcrafters.processengineapi.task.support

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}
/**
 * A simple component for holding user task information and payload of tasks.
 * May be used inside task delivery handlers and/or testing or registered to act as the main task handler and task termination handler.
 * For use as a main handler, the user task support offers additional registration methods for additional handlers to plug-in further
 * functionality.
 *
 * @since 0.1.2
 */
class UserTaskSupport {

  private lateinit var subscription: TaskSubscription
  private val payload: ConcurrentHashMap<String, Map<String, Any>> = ConcurrentHashMap()
  private val information: ConcurrentHashMap<String, TaskInformation> = ConcurrentHashMap()
  private val compositeTaskHandler: CompositeTaskHandler = CompositeTaskHandler().withHandler(this::onTaskDeliveryInternal)
  private val compositeTaskTerminationHandler: CompositeTaskTerminationHandler = CompositeTaskTerminationHandler().withHandler(this::onTaskRemovalInternal)

  init {
      logger.info { "PROCESS-ENGINE-API-014: Initialized user task support."   }
  }

  /**
   * Retrieves payload for task.
   * @param taskId task id of the task to query for.
   * @return payload.
   * @throws IllegalArgumentException if no task with given id is found.
   */
  @Throws(IllegalArgumentException::class)
  fun getPayload(taskId: String): Map<String, Any> {
    return requireNotNull(payload[taskId]) { "Could not find any variables for task $taskId." }
  }

  /**
   * Checks if the task with given id exists.
   * @param taskId id of the task.
   * @return true if task exists.
   */
  fun exists(taskId: String): Boolean = information.containsKey(taskId)

  /**
   * Retrieve information about a task.
   * @param taskId task id of interest.
   * @return task information.
   * @throws IllegalArgumentException if no task is found.
   */
  @Throws(IllegalArgumentException::class)
  fun getTaskInformation(taskId: String): TaskInformation = requireNotNull(information[taskId]) { "Could not find task $taskId." }

  /**
   * Retrieves the list of all task information items.
   * @return list of task information items.
   */
  fun getAllTasks(): List<TaskInformation> = information.values.toList()

  /**
   * Checks if the task with given id exists matching the task description key (activity id from XML) .
   * @param taskId id of the task.
   * @param activityId activity id from XML.
   * @return true if task exists.
   */
  fun exists(taskId: String, activityId: String?) =
    information.containsKey(taskId) && information[taskId]?.meta?.get(CommonRestrictions.ACTIVITY_ID) == activityId

  /**
   * Requires task to exist having the given id and task description key.
   * @param taskId id of the task.
   * @param activityId task description key (activity id from XML).
   * @throws IllegalArgumentException if no task can be found.
   */
  @Throws(IllegalArgumentException::class)
  fun requireTask(taskId: String, activityId: String) {
    require(exists(taskId, activityId)) { "Could not find task $taskId of type $activityId" }
  }


  /**
   * Adds handler.
   * @param taskHandler handler to add to the composite task handler.
   */
  fun addHandler(taskHandler: TaskHandler) {
    this.compositeTaskHandler.addHandler(taskHandler)
  }

  /**
   * Adds termination handler.
   * @param taskTerminationHandler termination handler to add to the composite task handler.
   */
  fun addTerminationHandler(taskTerminationHandler: TaskTerminationHandler) {
    this.compositeTaskTerminationHandler.addHandler(taskTerminationHandler)
  }

  /**
   * Subscribes current instance to receive tasks using embedded composite handlers.
   * @param taskSubscriptionApi API to subscribe to.
   * @param restrictions restrictions for subscription, see [CommonRestrictions.builder]
   * @param taskDescriptionKey optional task description key, defaults to null.
   * @param payloadDescription optional payload descriptions, defaults to null (no limitations, all variables).
   */
  fun subscribe(
      taskSubscriptionApi: TaskSubscriptionApi,
      restrictions: Map<String, String> = CommonRestrictions.builder().build(),
      taskDescriptionKey: String? = null,
      payloadDescription: Set<String>? = null
  ) {
    this.subscription = taskSubscriptionApi.subscribeForTask(
        SubscribeForTaskCmd(
            restrictions = restrictions,
            taskType = TaskType.USER,
            taskDescriptionKey = taskDescriptionKey,
            payloadDescription = payloadDescription,
            this::onTaskDelivery,
            this::onTaskRemoval,
        )
    ).get()
      .also { subscription ->
        logger.info { "PROCESS-ENGINE-API-010: Creating a new subscription for user task support: $subscription." }
      }
  }

  /**
   * Unsubscribes from task delivery.
   * @param taskSubscriptionApi API to unsubscribe from.
   */
  fun unsubscribe(taskSubscriptionApi: TaskSubscriptionApi) {
    if (this::subscription.isInitialized) {
      taskSubscriptionApi.unsubscribe(
          UnsubscribeFromTaskCmd(
              subscription = subscription,
          )
      )
      logger.info { "PROCESS-ENGINE-API-011: Unsubscribed user task support: $subscription." }
    }
  }

  /**
   * React to task arrival.
   * @param taskInformation task information.
   * @param taskPayload payload of the task.
   */
  fun onTaskDelivery(taskInformation: TaskInformation, taskPayload: Map<String, Any>) = compositeTaskHandler.accept(taskInformation, taskPayload)


  /**
   * React on task deletion.
   * @param taskId id of the task.
   */
  fun onTaskRemoval(taskId: String) = compositeTaskTerminationHandler.accept(taskId)


  /**
   * Store task information and payload on task arrival internally.
   * @param taskInformation task information.
   * @param taskPayload payload of the task.
   */
  private fun onTaskDeliveryInternal(taskInformation: TaskInformation, taskPayload: Map<String, Any>) {
    if (information.containsKey(taskInformation.taskId)) {
      logger.debug { "PROCESS-ENGINE-API-012: Received update for known task ${taskInformation.taskId}." }
      logger.trace { "PROCESS-ENGINE-API-013: Payload for task ${taskInformation.taskId} is $payload" }
    } else {
      logger.debug { "PROCESS-ENGINE-API-013: Received new task $taskInformation" }
    }
    information[taskInformation.taskId] = taskInformation
    payload[taskInformation.taskId] = taskPayload
  }

  /**
   * Cleanup task information and payload internally.
   * @param taskId id of the task.
   */
  private fun onTaskRemovalInternal(taskId: String) {
    information.remove(taskId)
    payload.remove(taskId)
    logger.trace { "PROCESS-ENGINE-API-014: Removed task $taskId" }
  }

}
