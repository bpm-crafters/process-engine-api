package dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.*
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class SignalApiImpl(
  private val runtimeService: RuntimeService
) : SignalApi {

  override fun sendSignal(cmd: SendSignalCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      val correlation = cmd.correlation
      runtimeService
        .createSignalEvent(cmd.signalName)
        .buildCorrelation(correlation)
        .setVariables(cmd.payloadSupplier.get())
        .send()
      Empty
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.PROCESS_INSTANCE_ID,
    CommonRestrictions.TENANT_ID,
  )

  private fun SignalEventReceivedBuilder.buildCorrelation(correlation: CorrelationSupplier) = this.apply {
    val restrictions = correlation.get().restrictions
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.TENANT_ID -> this.tenantId(value)
          CommonRestrictions.EXECUTION_ID -> this.executionId(value)
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
