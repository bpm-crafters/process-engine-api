package dev.bpmcrafters.processengineapi.adapter.c8.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep2
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val zeebeClient: ZeebeClient
) : CorrelationApi {

  override fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      val restrictions = cmd.correlation.get().restrictions
      ensureSupported(restrictions)
      zeebeClient
        .newPublishMessageCommand()
        .messageName(cmd.messageName)
        .withCorrelationKey(restrictions)
        .buildCorrelation(restrictions)
        .variables(cmd.payloadSupplier.get())
        .send()
        .get() // FIXME Chain
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
        .get() // FIXME Chain
      Empty
    }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  private fun PublishMessageCommandStep2.withCorrelationKey(restrictions: Map<String, String>): PublishMessageCommandStep3 {
    require(restrictions.containsKey(CommonRestrictions.CORRELATION_KEY)) { "${CommonRestrictions.CORRELATION_KEY} is mandatory, but was missing." }
    return this.correlationKey(restrictions[CommonRestrictions.CORRELATION_KEY])
  }

  private fun PublishMessageCommandStep3.buildCorrelation(restrictions: Map<String, String>): PublishMessageCommandStep3 = this.apply {
    if (restrictions.containsKey(CommonRestrictions.TENANT_ID)) {
      this.tenantId(restrictions[CommonRestrictions.TENANT_ID])
    }
    if (restrictions.containsKey(CommonRestrictions.MESSAGE_ID)) {
      this.messageId(restrictions[CommonRestrictions.MESSAGE_ID])
    }
    if (restrictions.containsKey(CommonRestrictions.MESSAGE_ID)) {
      this.variables(restrictions[CommonRestrictions.MESSAGE_ID])
    }
    if (restrictions.containsKey(CommonRestrictions.MESSAGE_TTL)) {
      this.variables(restrictions[CommonRestrictions.MESSAGE_TTL])
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.CORRELATION_KEY,
    CommonRestrictions.TENANT_ID,
    CommonRestrictions.MESSAGE_ID,
    CommonRestrictions.MESSAGE_TTL
  )
}
