package dev.bpmcrafters.processengineapi.adapter.c7.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.Correlation
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val runtimeService: RuntimeService
) : CorrelationApi {

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      runtimeService
        .createMessageCorrelation(cmd.messageName)
        .buildCorrelation(cmd.correlation)
        .setVariables(cmd.payloadSupplier.get())
        .correlate()
      Empty
    }
  }

  override fun sendSignal(cmd: SendSignalCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      runtimeService
        .createSignalEvent(cmd.signalName)
        .buildCorrelation(cmd.correlation)
        .setVariables(cmd.payloadSupplier.get())
        .send()
      Empty
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.PROCESS_INSTANCE_ID,
    CommonRestrictions.TENANT_ID,
  )

  private fun SignalEventReceivedBuilder.buildCorrelation(correlation: () -> Correlation) = this.apply {
    val restrictions = correlation.invoke().restrictions
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.TENANT_ID -> this.tenantId(value)
//          CommonRestrictions.EXECUTION_ID -> this.executionId(value)
        }
      }
  }

  private fun MessageCorrelationBuilder.buildCorrelation(correlation: () -> Correlation): MessageCorrelationBuilder = this.apply {
    val restrictions = correlation.invoke().restrictions
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.PROCESS_INSTANCE_ID -> this.processInstanceId(value)
          CommonRestrictions.TENANT_ID -> this.tenantId(value)
          // FIXME -> much more correlations are supported!
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
