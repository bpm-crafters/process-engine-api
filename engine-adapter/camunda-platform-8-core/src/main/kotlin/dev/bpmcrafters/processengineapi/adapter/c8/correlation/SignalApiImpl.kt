package dev.bpmcrafters.processengineapi.adapter.c8.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.command.BroadcastSignalCommandStep1
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

class SignalApiImpl(
  private val zeebeClient: ZeebeClient
) : SignalApi {

  override fun sendSignal(cmd: SendSignalCmd): Future<Empty> {
    return CompletableFuture.supplyAsync {
      logger.debug { "PROCESS-ENGINE-C8-002: Sending signal ${cmd.signalName}." }
      zeebeClient
        .newBroadcastSignalCommand()
        .signalName(cmd.signalName)
        .applyRestrictions(cmd.restrictions)
        .variables(cmd.payloadSupplier.get())
        .send()
        .get() // FIXME Chain
      Empty
    }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf(
    CommonRestrictions.TENANT_ID,
  )

  fun BroadcastSignalCommandStep1.BroadcastSignalCommandStep2.applyRestrictions(restrictions: Map<String, String>) = this.apply {
    if (restrictions.containsKey(CommonRestrictions.TENANT_ID)) {
      this.tenantId(restrictions[CommonRestrictions.TENANT_ID])
    }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }
}
