package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.extension

import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl
import org.camunda.bpm.client.impl.ExternalTaskClientImpl
import org.camunda.bpm.client.task.ExternalTaskService
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl

/**
 * External task client builder constructing extended client.
 */
class ExtendedExternalTaskClientBuilder : ExternalTaskClientBuilderImpl() {

  override fun build(): ExtendedExternalTaskClient {
    val delegate = super.build()
    require(delegate is ExternalTaskClientImpl) { "Delegate must be the official Camunda External Task Client" }
    val externalTaskService: ExternalTaskService = ExternalTaskServiceImpl(engineClient);
    return ExtendedExternalTaskClient(delegate, externalTaskService)
  }
}
