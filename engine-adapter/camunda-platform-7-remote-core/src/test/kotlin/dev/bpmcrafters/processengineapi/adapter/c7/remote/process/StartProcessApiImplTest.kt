package dev.bpmcrafters.processengineapi.adapter.c7.remote.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageCmd
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.community.mockito.process.ProcessInstanceFake
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class StartProcessApiImplTest {

 @Mock
 private lateinit var runtimeService: RuntimeService

 @InjectMocks
 private lateinit var startProcessApi: StartProcessApiImpl

 @Test
 fun `should start process via definition without payload`() {
  // given
  val startProcessByDefinitionCmd = StartProcessByDefinitionCmd("definitionKey") { emptyMap() }
  val processInstance: ProcessInstance = ProcessInstanceFake.builder().id("someId").build()
  `when`(runtimeService.startProcessInstanceByKey(anyString(), anyMap())).thenReturn(processInstance)

  // when
  startProcessApi.startProcess(startProcessByDefinitionCmd).get()

  // then
  verify(runtimeService).startProcessInstanceByKey("definitionKey", emptyMap())
 }

 @Test
 fun `should start process via definition with payload and business key`() {
  // given
  val processInstance: ProcessInstance = ProcessInstanceFake.builder().id("someId").build()
  `when`(runtimeService.startProcessInstanceByKey(anyString(), anyOrNull(), anyMap())).thenReturn(processInstance)
  val startProcessByDefinitionCmd = StartProcessByDefinitionCmd("definitionKey") {
   mapOf(
    "key" to "value",
    CommonRestrictions.BUSINESS_KEY to "businessKey"
   )
  }

  // when
  startProcessApi.startProcess(startProcessByDefinitionCmd).get()

  // then
  verify(runtimeService).startProcessInstanceByKey(
   "definitionKey", "businessKey", mapOf(
    "key" to "value",
    CommonRestrictions.BUSINESS_KEY to "businessKey"
   )
  )
 }

 @Test
 fun `should start process via message with business key`() {
  // given
  val payload = mapOf(CommonRestrictions.BUSINESS_KEY to "testBusinessKey", "key" to "value")
  val startProcessByMessageCmd = StartProcessByMessageCmd("testMessage") { payload }
  val correlationBuilder = messageCorrelationMock()
  `when`(runtimeService.createMessageCorrelation(any())).thenReturn(correlationBuilder)

  // When
  startProcessApi.startProcess(startProcessByMessageCmd).get()

  // Then
  verify(runtimeService).createMessageCorrelation("testMessage")
  verify(correlationBuilder).processInstanceBusinessKey("testBusinessKey")
  verify(correlationBuilder).setVariables(mapOf(CommonRestrictions.BUSINESS_KEY to "testBusinessKey", "key" to "value"))
 }

 @Test
 fun `should start process via message with payload`() {
  // given
  val payload = mapOf("key" to "value")
  val startProcessByMessageCmd = StartProcessByMessageCmd("testMessage") { payload }
  val correlationBuilder = messageCorrelationMock()
  `when`(runtimeService.createMessageCorrelation(any())).thenReturn(correlationBuilder)

  // When
  startProcessApi.startProcess(startProcessByMessageCmd).get()

  // Then
  verify(runtimeService).createMessageCorrelation("testMessage")
  verify(correlationBuilder).setVariables(mapOf("key" to "value"))
  verify(correlationBuilder, times(0)).processInstanceBusinessKey(any())
 }

 private fun messageCorrelationMock(): MessageCorrelationBuilder {
  val builder: MessageCorrelationBuilder = mock()
  lenient().whenever(builder.processInstanceBusinessKey(any())).thenReturn(builder)
  whenever(builder.setVariables(anyMap())).thenReturn(builder)
  whenever(builder.correlateStartMessage()).thenReturn(ProcessInstanceFake.builder().id("someId").build())

  return builder
 }
}