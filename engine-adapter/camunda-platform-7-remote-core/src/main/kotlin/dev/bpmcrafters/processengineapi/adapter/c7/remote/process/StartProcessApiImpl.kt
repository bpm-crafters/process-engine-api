package dev.bpmcrafters.processengineapi.adapter.c7.remote.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.process.*
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class StartProcessApiImpl(
  private val runtimeService: RuntimeService
) : StartProcessApi {

  companion object: KLogging()

  override fun startProcess(cmd: StartProcessCommand): Future<ProcessInformation> {
    return when (cmd) {
      is StartProcessByDefinitionCmd ->
        CompletableFuture.supplyAsync {
          logger.debug { "PROCESS-ENGINE-C7-REMOTE-004: starting a new process instance by definition ${cmd.definitionKey}." }
          runtimeService.startProcessInstanceByKey(
            cmd.definitionKey,
            cmd.payloadSupplier.get()
          ).toProcessInformation()
        }

      is StartProcessByMessageCmd ->
        CompletableFuture.supplyAsync {
          logger.debug { "PROCESS-ENGINE-C7-REMOTE-005: starting a new process instance by message ${cmd.messageName}." }
          runtimeService
            .createMessageCorrelation(cmd.messageName)
            .setVariables(cmd.payloadSupplier.get())
            .correlateStartMessage()
            .toProcessInformation()
        }

      else -> throw IllegalArgumentException("Unsupported start command $cmd")
    }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO()
  }
}

fun ProcessInstance.toProcessInformation() = ProcessInformation(
  instanceId = this.id,
  meta = mapOf(
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionId,
    CommonRestrictions.BUSINESS_KEY to this.businessKey,
    CommonRestrictions.TENANT_ID to this.tenantId,
    "rootProcessInstanceId" to this.rootProcessInstanceId
  )
)
