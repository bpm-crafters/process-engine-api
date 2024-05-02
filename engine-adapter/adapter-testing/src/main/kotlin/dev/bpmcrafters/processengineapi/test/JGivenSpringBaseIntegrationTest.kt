package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest
import com.tngtech.jgiven.junit5.ScenarioTest

abstract class JGivenSpringBaseIntegrationTest(
    @ProvidedScenarioState open val processTestHelper: ProcessTestHelper
) : SpringScenarioTest<BaseGivenWhenStage, BaseGivenWhenStage, BaseThenStage>()
