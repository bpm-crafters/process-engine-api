package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.EnableJGiven
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.test.BaseGivenWhenStage
import dev.bpmcrafters.processengineapi.test.BaseThenStage
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.camunda.bpm.client.spi.DataFormatConfigurator
import org.camunda.bpm.client.variable.impl.format.json.JacksonJsonDataFormat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.EnableCamundaRestClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.text.SimpleDateFormat

@SpringBootTest(
  classes = [TestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("itest")
@DirtiesContext
@Testcontainers
class C7RemoteStartProcessApiITest : SpringScenarioTest<BaseGivenWhenStage, BaseGivenWhenStage, BaseThenStage>() {

  @Container
  val camundaContainer = Camunda7RunTestContainer("run-7.21.0")

  companion object {
    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"
  }

  @Autowired
  @ScenarioState
  lateinit var startProcessApiImpl: StartProcessApi

  @Autowired
  @ScenarioState
  lateinit var processTestHelper: ProcessTestHelper

  @Autowired
  lateinit var repositoryService: RepositoryService

  @BeforeEach
  fun setUp() {
    repositoryService.createDeployment()
      .name("Simple Process")
      .addClasspathResource("bpmn/simple-process.bpmn")
      .deploy()
  }

  @Test
  fun `should start process by definition without payload`() {
    var list = repositoryService.createProcessDefinitionQuery().list()

    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process by definition with payload`() {
    WHEN
      .`start process by definition with payload`(KEY, "key" to "value")

    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process via message without payload`() {
    WHEN
      .`start process by message`(START_MESSAGE)

    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process via message with payload`() {
    WHEN
      .`start process by message with payload`(START_MESSAGE, "key" to "value")

    THEN
      .`we should have a running process`()
  }

}

@EnableJGiven
@EnableCamundaRestClient
@SpringBootApplication
class TestApplication {

  @Bean
  fun objectMapper(): ObjectMapper = JacksonDataFormatConfigurator.configureObjectMapper(ObjectMapper())

  @Bean
  fun processTestHelper(runtimeService: RuntimeService): ProcessTestHelper = C7RemoteProcessTestHelper(runtimeService)

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
