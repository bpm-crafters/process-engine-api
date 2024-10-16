package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest
import com.tngtech.jgiven.junit5.ScenarioTest

abstract class JGivenSpringBaseIntegrationTest : SpringScenarioTest<BaseGivenWhenStage, BaseGivenWhenStage, BaseThenStage>() {
  @ProvidedScenarioState
  open lateinit var processTestHelper: ProcessTestHelper
}
