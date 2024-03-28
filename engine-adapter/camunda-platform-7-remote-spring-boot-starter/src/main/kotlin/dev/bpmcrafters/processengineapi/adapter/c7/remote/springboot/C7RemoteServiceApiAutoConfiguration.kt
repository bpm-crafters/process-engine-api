package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.EnableCamundaRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(C7RemoteAdapterAutoConfiguration::class)
@EnableCamundaRestClient
class C7RemoteServiceApiAutoConfiguration {

  @Bean
  fun startProcessApi(@Qualifier("remote") runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun correlationApi(@Qualifier("remote") runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun signalApi(@Qualifier("remote") runtimeService: RuntimeService): SignalApi = SignalApiImpl(
    runtimeService = runtimeService
  )

}
