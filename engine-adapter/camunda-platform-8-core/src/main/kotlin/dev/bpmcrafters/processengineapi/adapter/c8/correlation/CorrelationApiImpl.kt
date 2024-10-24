package dev.bpmcrafters.processengineapi.adapter.c8.correlation

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import io.camunda.zeebe.client.ZeebeClient
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val zeebeClient: ZeebeClient
) : CorrelationApi {

  companion object : KLogging()

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      val correlationKey = cmd.correlation.get().correlationKey
      logger.debug { "PROCESS-ENGINE-C8-001: Correlating message ${cmd.messageName} using correlation key value $correlationKey." }
      zeebeClient
        .newPublishMessageCommand()
        .messageName(cmd.messageName)
        .correlationKey(correlationKey)
        .variables(cmd.payloadSupplier.get())
        .send()
        .get() // FIXME Chain
      Empty
    }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  override fun getSupportedRestrictions(): Set<String> = setOf()
}
