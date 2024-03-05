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

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.PROCESS_INSTANCE_ID,
    CommonRestrictions.TENANT_ID,
    CommonRestrictions.BUSINESS_KEY,
    "processDefinitionId"
  )

  private fun MessageCorrelationBuilder.buildCorrelation(correlation: CorrelationSupplier): MessageCorrelationBuilder = this.apply {
    val restrictions = correlation.get().restrictions
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.TENANT_ID -> this.tenantId(value)
          CommonRestrictions.PROCESS_INSTANCE_ID -> this.processInstanceId(value)
          CommonRestrictions.BUSINESS_KEY -> this.processInstanceBusinessKey(value)
          "processDefinitionId" -> this.processDefinitionId(value)
          // FIXME -> much more correlations are supported!
          // FIXME -> how to handle "localVariableEquals? use separator like name=value?
          // FIXME -> how to handle "processVariableEquals? use separator like name=value?
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
