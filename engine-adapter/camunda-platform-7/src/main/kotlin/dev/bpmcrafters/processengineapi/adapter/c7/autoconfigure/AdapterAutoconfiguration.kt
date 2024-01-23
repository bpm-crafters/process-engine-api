package dev.bpmcrafters.processengineapi.adapter.c7.autoconfigure

import dev.bpmcrafters.processengineapi.adapter.c7.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.TaskApiImpl
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskApi
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RuntimeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class AdapterAutoconfiguration {

  @Bean
  fun startProcessApi(runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun taskApi(externalTaskService: ExternalTaskService): TaskApi = TaskApiImpl(
    externalTaskService = externalTaskService
  )

  @Bean
  fun correlationApi(runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )
}
