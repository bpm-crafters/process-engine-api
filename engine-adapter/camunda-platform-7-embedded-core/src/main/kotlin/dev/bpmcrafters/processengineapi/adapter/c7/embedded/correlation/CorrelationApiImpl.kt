package dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.adapter.commons.task.ParsingHelper
import dev.bpmcrafters.processengineapi.correlation.*
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CorrelationApiImpl(
  private val runtimeService: RuntimeService,
  private val parsingHelper: ParsingHelper
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
    CommonRestrictions.PROCESS_DEFINITION_ID,
    CommonRestrictions.TENANT_ID,
    CommonRestrictions.BUSINESS_KEY,
    CommonRestrictions.PROCESS_VARIABLE_LIST,
    CommonRestrictions.PROCESS_VARIABLE_LIST_JSON,
    CommonRestrictions.LOCAL_VARIABLE_LIST,
    CommonRestrictions.LOCAL_VARIABLE_LIST_JSON,
  )

  private fun MessageCorrelationBuilder.buildCorrelation(correlation: CorrelationSupplier): MessageCorrelationBuilder = this.apply {
    val restrictions = correlation.get().restrictions
    ensureSupported(restrictions)
    restrictions
      .forEach { (key, value) ->
        when (key) {
          CommonRestrictions.TENANT_ID -> this.tenantId(value).apply {
            require(restrictions.containsKey(CommonRestrictions.WITHOUT_TENANT_ID)) { "Illegal restriction combination. ${CommonRestrictions.WITHOUT_TENANT_ID} " +
              "and ${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided in the same time because they are mutually exclusive." }
          }
          CommonRestrictions.WITHOUT_TENANT_ID -> this.withoutTenantId().apply {
            require(restrictions.containsKey(CommonRestrictions.TENANT_ID)) { "Illegal restriction combination. ${CommonRestrictions.WITHOUT_TENANT_ID} " +
              "and ${CommonRestrictions.WITHOUT_TENANT_ID} can't be provided in the same time because they are mutually exclusive." }
          }
          CommonRestrictions.PROCESS_VARIABLE_LIST -> this.processInstanceVariablesEqual(
            parsingHelper.parseString(value)
          )
          CommonRestrictions.PROCESS_VARIABLE_LIST_JSON -> this.processInstanceVariablesEqual(
            parsingHelper.parseJsonString(value)
          )
          CommonRestrictions.LOCAL_VARIABLE_LIST -> this.processInstanceVariablesEqual(
            parsingHelper.parseString(value)
          )
          CommonRestrictions.LOCAL_VARIABLE_LIST_JSON -> this.processInstanceVariablesEqual(
            parsingHelper.parseJsonString(value)
          )
          CommonRestrictions.PROCESS_INSTANCE_ID -> this.processInstanceId(value)
          CommonRestrictions.PROCESS_DEFINITION_ID -> this.processDefinitionId(value)
          CommonRestrictions.BUSINESS_KEY -> this.processInstanceBusinessKey(value)
        }
      }
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

}
