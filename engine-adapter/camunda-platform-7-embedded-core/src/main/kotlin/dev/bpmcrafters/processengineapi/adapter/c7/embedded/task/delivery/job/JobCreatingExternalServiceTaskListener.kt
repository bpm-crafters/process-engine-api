package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_SERVICE
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener

private val logger = KotlinLogging.logger {}

/**
 * Task listener writing a job to notify delivery.
 */
open class JobCreatingExternalServiceTaskListener : ExecutionListener {

  override fun notify(delegateExecution: DelegateExecution) {
    when (delegateExecution.eventName) {
      ExecutionListener.EVENTNAME_START -> createJob(
        EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
          executionId = delegateExecution.id,
          type = TYPE_SERVICE,
          operation = OPERATION_CREATE,
          processDefinitionId = delegateExecution.processDefinitionId,
          processInstanceId = delegateExecution.processInstanceId
        )
      )

      ExecutionListener.EVENTNAME_END -> createJob(
        EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
          executionId = delegateExecution.id,
          type = TYPE_SERVICE,
          operation = OPERATION_DELETE,
          processDefinitionId = delegateExecution.processDefinitionId,
          processInstanceId = delegateExecution.processInstanceId
        )
      )
    }
  }
}
