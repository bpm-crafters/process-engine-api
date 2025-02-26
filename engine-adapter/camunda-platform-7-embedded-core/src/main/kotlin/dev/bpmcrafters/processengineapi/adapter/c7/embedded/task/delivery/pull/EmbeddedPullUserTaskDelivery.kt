package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * Delivers user tasks to subscriptions.
 * Uses internal Java API for pulling tasks.
 */
class EmbeddedPullUserTaskDelivery(
  private val taskService: TaskService,
  private val repositoryService: RepositoryService,
  private val subscriptionRepository: SubscriptionRepository,
  private val executorService: ExecutorService
) : UserTaskDelivery, RefreshableDelivery {

  private val cachingProcessDefinitionKeyResolver = CachingProcessDefinitionKeyResolver(repositoryService)

  /**
   * Delivers all tasks found in user task service to corresponding subscriptions.
   */
  override fun refresh() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions().filter { s -> s.taskType == TaskType.USER }
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-036: pulling user tasks for subscriptions: $subscriptions" }
      taskService
        .createTaskQuery()
        .initializeFormKeys()
        .forSubscriptions(subscriptions)
        .list()
        .parallelStream()
        .forEach { task ->
          subscriptions
            .firstOrNull { subscription -> subscription.matches(task) }
            ?.let { activeSubscription ->
              executorService.submit {  // in another thread
                subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)
                val variables = taskService.getVariables(task.id).filterBySubscription(activeSubscription)
                try {
                  logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-037: delivering user task ${task.id}." }
                  val processDefinitionKey = cachingProcessDefinitionKeyResolver.getProcessDefinitionKey(task.processDefinitionId)
                  activeSubscription.action.accept(task.toTaskInformation(taskService.getIdentityLinksForTask(task.id), processDefinitionKey), variables)
                } catch (e: Exception) {
                  logger.error { "PROCESS-ENGINE-C7-EMBEDDED-038: error delivering task ${task.id}: ${e.message}" }
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = task.id)
                }
              }
            }
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-039: pull user tasks disabled because of no active subscriptions" }
    }
  }

  private fun TaskQuery.forSubscriptions(@Suppress("UNUSED_PARAMETER") subscriptions: List<TaskSubscriptionHandle>): TaskQuery {
    // TODO: narrow down, for the moment take all tasks matching tenants
    return this
      .active()
    // FIXME -> consider complex tent filtering
  }


  private fun TaskSubscriptionHandle.matches(task: Task): Boolean =
    this.taskType == TaskType.USER && (
      this.taskDescriptionKey == null
        || this.taskDescriptionKey == task.taskDefinitionKey
        || this.taskDescriptionKey == task.id
      ) && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == task.executionId
        CommonRestrictions.TENANT_ID -> it.value == task.tenantId
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == task.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == task.processDefinitionId
        CommonRestrictions.PROCESS_DEFINITION_KEY -> it.value == cachingProcessDefinitionKeyResolver.getProcessDefinitionKey(task.processDefinitionId)
        CommonRestrictions.PROCESS_DEFINITION_VERSION_TAG -> it.value == cachingProcessDefinitionKeyResolver.getProcessDefinitionVersionTag(task.processDefinitionId)
        else -> false
      }
    }
}

