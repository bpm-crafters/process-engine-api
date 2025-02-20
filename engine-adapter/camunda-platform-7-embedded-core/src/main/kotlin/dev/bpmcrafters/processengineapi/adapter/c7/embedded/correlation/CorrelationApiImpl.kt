package dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.RuntimeService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

class CorrelationApiImpl(
  private val runtimeService: RuntimeService,
) : CorrelationApi {

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      val correlation = cmd.correlation.get()
      logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-001: Correlating message ${cmd.messageName} using local variable ${correlation.correlationVariable} with value ${correlation.correlationKey}" }
      runtimeService
        .createMessageCorrelation(cmd.messageName)
        .localVariableEquals(correlation.correlationVariable, correlation.correlationKey)
        .setVariables(cmd.payloadSupplier.get())
        .correlate()
      Empty
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    /*
    TODO really?
    CommonRestrictions.PROCESS_INSTANCE_ID,
    CommonRestrictions.PROCESS_DEFINITION_ID,
    CommonRestrictions.TENANT_ID,
    CommonRestrictions.BUSINESS_KEY
     */
  )

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
