package dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_SERVICE
import mu.KLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

/**
 * Task listener writing a job to notify delivery.
 */
open class JobCreatingExternalServiceTaskListener(
  private val processEngineConfigurationImpl: ProcessEngineConfigurationImpl
) : ExecutionListener {

  companion object : KLogging()

  override fun notify(delegateExecution: DelegateExecution) {
    when (delegateExecution.eventName) {
      ExecutionListener.EVENTNAME_START -> processEngineConfigurationImpl.createJob(
        EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
          id = delegateExecution.id,
          type = TYPE_SERVICE,
          operation = OPERATION_CREATE
        )
      )

      ExecutionListener.EVENTNAME_END -> processEngineConfigurationImpl.createJob(
        EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
          id = delegateExecution.id,
          type = TYPE_SERVICE,
          operation = OPERATION_DELETE
        )
      )
    }
  }
}
