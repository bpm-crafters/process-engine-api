package dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

class SignalApiImpl(
  private val runtimeService: RuntimeService
) : SignalApi {

  override fun sendSignal(cmd: SendSignalCmd): Future<Empty> {
    logger.debug { "PROCESS-ENGINE-C7-REMOTE-002: Sending signal ${cmd.signalName}." }
    return CompletableFuture.supplyAsync {
      runtimeService
        .createSignalEvent(cmd.signalName)
        .applyRestrictions(cmd.restrictions)
        .setVariables(cmd.payloadSupplier.get())
        .send()
      Empty
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.PROCESS_INSTANCE_ID,
    CommonRestrictions.TENANT_ID,
    CommonRestrictions.WITHOUT_TENANT_ID,
  )

  private fun SignalEventReceivedBuilder.applyRestrictions(restrictions: Map<String, String>) = this.apply {
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.TENANT_ID -> this.tenantId(value).apply {
            require(restrictions.containsKey(CommonRestrictions.WITHOUT_TENANT_ID)) { "Illegal restriction combination. ${CommonRestrictions.WITHOUT_TENANT_ID} " +
              "and ${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided in the same time because they are mutually exclusive." }
          }
          CommonRestrictions.WITHOUT_TENANT_ID -> this.withoutTenantId().apply {
            require(restrictions.containsKey(CommonRestrictions.TENANT_ID)) { "Illegal restriction combination. ${CommonRestrictions.WITHOUT_TENANT_ID} " +
              "and ${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided in the same time because they are mutually exclusive." }
          }
          CommonRestrictions.EXECUTION_ID -> this.executionId(value)
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
