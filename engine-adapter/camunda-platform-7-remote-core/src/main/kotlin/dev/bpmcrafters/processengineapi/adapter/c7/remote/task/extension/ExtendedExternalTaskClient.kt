package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.extension

import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.impl.ExternalTaskClientImpl
import org.camunda.bpm.client.task.ExternalTaskService

/**
 * Extended client allowing to complete task outside a handle method.
 */
class ExtendedExternalTaskClient(
  private val delegate: ExternalTaskClientImpl,
  private val externalTaskService: ExternalTaskService,
) : ExternalTaskClient by delegate, ExternalTaskService by externalTaskService
