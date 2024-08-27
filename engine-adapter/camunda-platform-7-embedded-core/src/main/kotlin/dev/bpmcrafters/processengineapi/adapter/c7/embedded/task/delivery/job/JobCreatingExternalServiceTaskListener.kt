package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_SERVICE
import mu.KLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener

/**
 * Task listener writing a job to notify delivery.
 */
open class JobCreatingExternalServiceTaskListener : ExecutionListener {

  companion object : KLogging()

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
