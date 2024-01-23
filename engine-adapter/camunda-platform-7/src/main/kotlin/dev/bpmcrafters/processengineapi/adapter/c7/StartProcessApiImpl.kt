package dev.bpmcrafters.processengineapi.adapter.c7

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.process.*
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class StartProcessApiImpl(
  private val runtimeService: RuntimeService
) : StartProcessApi {

  override fun startProcess(cmd: StartProcessCommand): Future<StartProcessResponse> {
    return when (cmd) {
      is StartProcessByDefinitionCmd ->
        CompletableFuture.supplyAsync {
          runtimeService.startProcessInstanceByKey(
            cmd.definitionKey,
            cmd.payloadSupplier.get()
          ).toResponse()
        }

      is StartProcessByMessageCmd ->
        CompletableFuture.supplyAsync {
          runtimeService
            .createMessageCorrelation(cmd.messageName)
            .setVariables(cmd.payloadSupplier.get())
            .correlateStartMessage()
            .toResponse()
        }

      else -> throw IllegalArgumentException("Unsupported start command $cmd")
    }
  }

  private fun ProcessInstance.toResponse() = StartProcessResponse(
    instanceId = this.id,
    meta = mapOf(
      "processDefinitionId" to this.processDefinitionId,
      "businessKey" to this.businessKey,
      "tenantId" to this.tenantId,
      "rootProcessInstanceId" to this.rootProcessInstanceId
    )
  )

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO()
  }
}
