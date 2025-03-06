package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.ExternalServiceTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.filterBySubscription
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * Delivers external tasks to subscriptions.
 * This implementation uses internal Java API and pulls tasks for delivery.
 */
class EmbeddedPullServiceTaskDelivery(
    private val externalTaskService: ExternalTaskService,
    private val workerId: String,
    private val subscriptionRepository: SubscriptionRepository,
    private val maxTasks: Int,
    private val lockDurationInSeconds: Long,
    private val retryTimeoutInSeconds: Long,
    private val retries: Int,
    private val executorService: ExecutorService
) : ExternalServiceTaskDelivery, RefreshableDelivery {

  /**
   * Delivers all tasks found in the external service to corresponding subscriptions.
   */
  override fun refresh() {

    val subscriptions = subscriptionRepository.getTaskSubscriptions().filter { s -> s.taskType == TaskType.EXTERNAL }
    if (subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-030: pulling service tasks for subscriptions: $subscriptions" }
      // TODO -> how many queries do we want? 1:1 subscriptions, or 1 query for all?
      externalTaskService
        .fetchAndLock(maxTasks, workerId)
        .forSubscriptions(subscriptions)
        .execute()
        .parallelStream()
        .forEach { lockedTask ->
          subscriptions
            .firstOrNull { subscription -> subscription.matches(lockedTask) }
            ?.let { activeSubscription ->
              executorService.submit {  // in another thread
                subscriptionRepository.activateSubscriptionForTask(lockedTask.id, activeSubscription)
                val variables = lockedTask.variables.filterBySubscription(activeSubscription)
                try {
                  logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-031: delivering service task ${lockedTask.id}." }
                  activeSubscription.action.accept(lockedTask.toTaskInformation(), variables)
                  logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-032: successfully delivered service task ${lockedTask.id}." }
                } catch (e: Exception) {
                  val jobRetries: Int = lockedTask.retries ?: retries
                  logger.error { "PROCESS-ENGINE-C7-EMBEDDED-033: failing delivering task ${lockedTask.id}: ${e.message}" }
                  externalTaskService.handleFailure(lockedTask.id, workerId, e.message, jobRetries - 1, retryTimeoutInSeconds * 1000)
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = lockedTask.id)
                  logger.error { "PROCESS-ENGINE-C7-EMBEDDED-034: successfully failed delivering task ${lockedTask.id}: ${e.message}" }
                }
              }.get()
            }
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-035: Pull external tasks disabled because of no active subscriptions" }
    }
  }

  private fun ExternalTaskQueryBuilder.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): ExternalTaskQueryBuilder {
    subscriptions
      .filter { it.taskDescriptionKey != null }
      .distinctBy { it.taskDescriptionKey  }
      .forEach { subscription ->
        this
          .topic(subscription.taskDescriptionKey, lockDurationInSeconds * 1000)
          .enableCustomObjectDeserialization()
        // FIXME -> consider complex tenant filtering
      }
    return this
  }

  private fun TaskSubscriptionHandle.matches(task: LockedExternalTask): Boolean {
    return this.taskType == TaskType.EXTERNAL
      && (this.taskDescriptionKey == null || this.taskDescriptionKey == task.topicName)
      && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == task.executionId
        CommonRestrictions.ACTIVITY_ID -> it.value == task.activityId
        CommonRestrictions.BUSINESS_KEY -> it.value == task.businessKey
        CommonRestrictions.TENANT_ID -> it.value == task.tenantId
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == task.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_KEY -> it.value == task.processDefinitionKey
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == task.processDefinitionId
        CommonRestrictions.PROCESS_DEFINITION_VERSION_TAG -> it.value == task.processDefinitionVersionTag
        else -> false
      }
    }
  }
}
