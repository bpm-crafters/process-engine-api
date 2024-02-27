package dev.bpmcrafters.processengineapi.adapter.c8.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd
import io.camunda.zeebe.client.ZeebeClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val zeebeClient: ZeebeClient
) : CorrelationApi {

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      val restrictions = cmd.correlation.invoke().restrictions
      ensureSupported(restrictions)
      zeebeClient
        .newPublishMessageCommand()
        .messageName(cmd.messageName)
        .correlationKey(restrictions[CommonRestrictions.CORRELATION_KEY])
        .variables(cmd.payloadSupplier.get())
        // FIXME -> tenant
        // FIXME -> messageId? what is it?!
        .send().get() // FIXME Chain
      Empty
    }
  }

  override fun sendSignal(cmd: SendSignalCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      zeebeClient
        .newBroadcastSignalCommand()
        .signalName(cmd.signalName)
        .variables(cmd.payloadSupplier.get())
        .send()
      Empty
    }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(CommonRestrictions.CORRELATION_KEY)

}
