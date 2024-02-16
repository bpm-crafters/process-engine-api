package dev.bpmcrafters.processengineapi.adapter.c7.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.Correlation
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val runtimeService: RuntimeService
) : CorrelationApi {

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Unit> {
    return CompletableFuture.supplyAsync {
      runtimeService
        .createMessageCorrelation(cmd.messageName)
        .buildCorrelation(cmd.correlation)
        .setVariables(cmd.payloadSupplier.get())
        .correlate()
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(CommonRestrictions.PROCESS_INSTANCE_ID)

  private fun MessageCorrelationBuilder.buildCorrelation(correlation: () -> Correlation): MessageCorrelationBuilder = this.apply {
    val restrictions = correlation.invoke().restrictions
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.PROCESS_INSTANCE_ID -> this.processInstanceId(value)
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
