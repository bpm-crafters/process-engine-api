package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.junit5.ScenarioTest

abstract class JGivenBaseIntegrationTest(
    @ProvidedScenarioState open val processTestHelper: ProcessTestHelper
) : ScenarioTest<BaseGivenWhenStage, BaseGivenWhenStage, BaseThenStage>()
