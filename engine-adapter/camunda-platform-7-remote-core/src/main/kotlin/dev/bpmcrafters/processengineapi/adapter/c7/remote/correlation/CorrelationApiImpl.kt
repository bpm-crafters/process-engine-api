package dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val runtimeService: RuntimeService
) : CorrelationApi {

  companion object: KLogging()

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      val correlation = cmd.correlation.get()
      logger.debug { "PROCESS-ENGINE-C7-REMOTE-001: Correlating message ${cmd.messageName} using local variable ${correlation.correlationVariable} with value ${correlation.correlationKey}" }
      runtimeService
        .createMessageCorrelation(cmd.messageName)
        .localVariableEquals(correlation.correlationVariable, correlation.correlationKey)
        .setVariables(cmd.payloadSupplier.get())
        .correlate()
      Empty
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf()

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
