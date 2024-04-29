package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.tngtech.jgiven.integration.spring.EnableJGiven
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullExternalTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.client.spi.DataFormatConfigurator
import org.camunda.bpm.client.variable.impl.format.json.JacksonJsonDataFormat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.EnableCamundaRestClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.text.SimpleDateFormat


@EnableJGiven
@EnableCamundaRestClient
@SpringBootApplication
class C7RemoteTestApplication {

  @Bean
  fun objectMapper(): ObjectMapper = JacksonDataFormatConfigurator.configureObjectMapper(ObjectMapper())

  @Bean
  fun processTestHelper(
    runtimeService: RuntimeService,
    startProcessApi: StartProcessApi,
    taskSubscriptionApi: TaskSubscriptionApi,
    userTaskDelivery: UserTaskDelivery,
    externalTaskDelivery: RemotePullExternalTaskDelivery,
    userTaskCompletionApi: UserTaskCompletionApi,
    externalTaskCompletionApi: ExternalTaskCompletionApi,
    subscriptionRepository: SubscriptionRepository,
  ): ProcessTestHelper = C7RemoteProcessTestHelper(
    runtimeService = runtimeService,
    startProcessApi = startProcessApi,
    taskSubscriptionApi = taskSubscriptionApi,
    userTaskDelivery = userTaskDelivery,
    externalTaskDelivery = externalTaskDelivery,
    userTaskCompletionApi = userTaskCompletionApi,
    externalTaskCompletionApi = externalTaskCompletionApi,
    subscriptionRepository = subscriptionRepository
  )

}

/**
 * Configured SPIN Jackson Mapper.
 * Don't forget to look into **META-INF/services**
 */
class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {

  companion object {
    fun configureObjectMapper(objectMapper: ObjectMapper) = objectMapper.apply {
      registerModule(KotlinModule())
      registerModule(Jdk8Module())
      registerModule(JavaTimeModule())
      dateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:MM:ss.SSSz")
    }
  }

  override fun configure(dataFormat: JacksonJsonDataFormat) {
    configureObjectMapper(dataFormat.objectMapper)
  }

  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> {
    return JacksonJsonDataFormat::class.java
  }
}
