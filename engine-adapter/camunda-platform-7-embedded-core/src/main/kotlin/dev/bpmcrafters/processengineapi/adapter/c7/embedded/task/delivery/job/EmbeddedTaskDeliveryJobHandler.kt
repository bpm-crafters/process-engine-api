package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_SERVICE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_USER
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.externaltask.LockedExternalTaskImpl
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.*
import dev.bpmcrafters.processengineapi.impl.task.filterBySubscription
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Job handler delivering task creating, modification and deletion.
 */
class EmbeddedTaskDeliveryJobHandler(
    private val subscriptionRepository: SubscriptionRepository,
    private val workerId: String,
    private val lockTimeInSeconds: Long
) : JobHandler<EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration> {

  companion object {
    const val TYPE = "dev.bpm-crafters.processengineapi.EmbeddedTaskDeliveryJobHandler"
  }

  override fun execute(
    configuration: EmbeddedTaskDeliveryJobHandlerConfiguration, execution: ExecutionEntity?, commandContext: CommandContext, tenantId: String?
  ) {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    when (configuration.operation) {
      OPERATION_CREATE -> when (configuration.type) {
        TYPE_USER -> {
          val userTask = commandContext.taskManager.findTaskById(configuration.executionId)
          logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-020: Delivering user task for execution ${configuration.executionId}, with task ${userTask.id}" }
          subscriptions.firstOrNull { subscription -> subscription.matches(userTask) }?.let { activeSubscription ->
            logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-021: Found subscription for user task." }
            subscriptionRepository.activateSubscriptionForTask(userTask.id, activeSubscription)
            val variables = userTask.variables.filterBySubscription(activeSubscription)
            try {
              activeSubscription.action.accept(userTask.toTaskInformation(), variables)
            } catch (e: Exception) {
              logger.error { "PROCESS-ENGINE-C7-EMBEDDED-022: Error delivering task ${userTask.id}: ${e.message}" }
              subscriptionRepository.deactivateSubscriptionForTask(taskId = userTask.id)
            }
          }
        }

        TYPE_SERVICE -> {
          val serviceTask = commandContext.fetchAndLockExternalTask(configuration.executionId, workerId, lockTimeInSeconds * 1000)
          logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-023: Delivering external service task for execution ${configuration.executionId}, with task ${serviceTask.id}" }
          subscriptions.firstOrNull { subscription -> subscription.matches(serviceTask) }?.let { activeSubscription ->
            try {
              logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-024: Found subscription for topic '${activeSubscription.taskDescriptionKey}'" }
              subscriptionRepository.activateSubscriptionForTask(taskId = serviceTask.id, subscription = activeSubscription)
              val variables = serviceTask.variables.filterBySubscription(activeSubscription)
              activeSubscription.action.accept(serviceTask.toTaskInformation(), variables)
            } catch (e: Exception) {
              logger.error { "PROCESS-ENGINE-C7-EMBEDDED-025: Error delivering task ${serviceTask.id}: ${e.message}" }
              subscriptionRepository.deactivateSubscriptionForTask(taskId = serviceTask.id)
            }
          }
        }
      }

      OPERATION_DELETE -> subscriptionRepository.getActiveSubscriptionForTask(configuration.executionId)?.apply {
        termination.accept(configuration.executionId)
      }
    }
  }

  override fun onDelete(configuration: EmbeddedTaskDeliveryJobHandlerConfiguration, jobEntity: JobEntity) {
    // no op?
  }

  override fun newConfiguration(canonicalString: String): EmbeddedTaskDeliveryJobHandlerConfiguration =
    EmbeddedTaskDeliveryJobHandlerConfiguration.new(canonicalString)

  override fun getType(): String = TYPE

  private fun TaskSubscriptionHandle.matches(taskEntity: TaskEntity): Boolean =
    this.taskType == TaskType.USER && (this.taskDescriptionKey == null || this.taskDescriptionKey == taskEntity.taskDefinitionKey || this.taskDescriptionKey == taskEntity.id)
      && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == taskEntity.executionId
        CommonRestrictions.TENANT_ID -> it.value == taskEntity.tenantId
        CommonRestrictions.ACTIVITY_ID -> it.value == taskEntity.taskDefinitionKey
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == taskEntity.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == taskEntity.processDefinitionId
        // FIXME -> process_defintion_key
        else -> false
      }
    }

  private fun TaskSubscriptionHandle.matches(taskEntity: LockedExternalTask): Boolean =
    this.taskType == TaskType.EXTERNAL && (this.taskDescriptionKey == null || this.taskDescriptionKey == taskEntity.topicName)
      && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == taskEntity.executionId
        CommonRestrictions.TENANT_ID -> it.value == taskEntity.tenantId
        CommonRestrictions.ACTIVITY_ID -> it.value == taskEntity.activityId
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == taskEntity.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == taskEntity.processDefinitionId
        // FIXME -> process_defintion_key
        else -> false
      }
    }

  /**
   * Job configuration.
   */
  data class EmbeddedTaskDeliveryJobHandlerConfiguration(
    val executionId: String,
    val type: String,
    val operation: String,
    val processDefinitionId: String,
    val processInstanceId: String
  ) : JobHandlerConfiguration {

    companion object {
      const val SEPARATOR = "#"

      const val OPERATION_CREATE = "create"
      const val OPERATION_DELETE = "delete"

      const val TYPE_USER = "user"
      const val TYPE_SERVICE = "service"

      @JvmStatic
      fun new(canonicalString: String) = canonicalString.split(SEPARATOR).let { parameters ->
        EmbeddedTaskDeliveryJobHandlerConfiguration(
          executionId = parameters[0],
          type = parameters[1],
          operation = parameters[2],
          processDefinitionId = parameters[3],
          processInstanceId = parameters[4]
        )
      }
    }

    override fun toCanonicalString(): String =
      arrayOf(
        executionId,
        type,
        operation,
        processDefinitionId,
        processInstanceId,
      ).joinToString(SEPARATOR)
  }
}

/**
 * Creates a new job.
 */
fun createJob(
  configuration: EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration,
  context: CommandContext = Context.getCommandContext()
) {
  context.dbEntityManager.insert(
    MessageEntity().apply {
      jobHandlerConfigurationRaw = configuration.toCanonicalString()
      jobHandlerType = EmbeddedTaskDeliveryJobHandler.TYPE
      duedate = Date.from(Instant.now())
      // we don't want to retry the delivery for the moment.
      setRetriesFromPersistence(1)
    }
  )
}

/**
 * References external task service via process engine configuration of the command context.
 */
val CommandContext.externalTaskService: ExternalTaskService get() = this.processEngineConfiguration.externalTaskService

/**
 * Fetches and locks external task for given execution id.
 * @param executionId execution id.
 * @param workerId worker id to lock the task for.
 * @param lockDuration lock duration in seconds.
 */
fun CommandContext.fetchAndLockExternalTask(executionId: String, workerId: String, lockDuration: Long): LockedExternalTask {
  val taskEntity: ExternalTaskEntity = requireNotNull(
    externalTaskService.createExternalTaskQuery().executionId(executionId).singleResult()
  ) { "ExternalTask for executionId=$executionId not found" } as ExternalTaskEntity

  externalTaskService.lock(taskEntity.id, workerId, lockDuration)

  return LockedExternalTaskImpl.fromEntity(
    taskEntity, null, false, true, false
  )
}

