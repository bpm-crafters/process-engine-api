package dev.bpmcrafters.processengineapi.adapter.c8.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.process.*
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent
import io.camunda.zeebe.client.api.response.PublishMessageResponse
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class StartProcessApiImpl(
  private val zeebeClient: ZeebeClient
) : StartProcessApi {

  companion object: KLogging()

  override fun startProcess(cmd: StartProcessCommand): Future<ProcessInformation> {
    return when (cmd) {
      is StartProcessByDefinitionCmd ->
        CompletableFuture.supplyAsync {
          logger.debug { "PROCESS-ENGINE-C8-004: Starting a new process instance by definition ${cmd.definitionKey}." }
          zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(cmd.definitionKey)
            .latestVersion()
            .variables(cmd.payloadSupplier.get())
            .send()
            .get()
            .toProcessInformation()
        }
      is StartProcessByMessageCmd ->
        CompletableFuture.supplyAsync {
          logger.debug { "PROCESS-ENGINE-C8-005: Starting a new process instance by message ${cmd.messageName}." }
          zeebeClient
            .newPublishMessageCommand()
            .messageName(cmd.messageName)
            .correlationKey("") // empty means create a new instance
            .variables(cmd.payloadSupplier.get())
            .send()
            .get()
            .toProcessInformation()
        }
      else -> throw IllegalArgumentException("Unsupported start command $cmd")
    }
  }

  private fun ProcessInstanceEvent.toProcessInformation() = ProcessInformation(
    instanceId = "${this.processInstanceKey}",
    meta = mapOf(
      "processDefinitionId" to "${this.processDefinitionKey}",
      CommonRestrictions.PROCESS_DEFINITION_KEY to this.bpmnProcessId,
      CommonRestrictions.TENANT_ID to this.tenantId,
    )
  )

  private fun PublishMessageResponse.toProcessInformation() = ProcessInformation(
    instanceId = "",
    meta = mapOf(

    )
  )

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }
}
