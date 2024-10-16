package dev.bpmcrafters.example.common.adapter.out.process;

import com.tngtech.jgiven.junit5.DualScenarioTest;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

@ZeebeProcessTest
public class SimpleProcessTest extends DualScenarioTest<SimpleProcessStages.ActionStage, SimpleProcessStages.AssertStage> {

  private ZeebeClient client;
  private ZeebeTestEngine engine;

  @BeforeEach
  public void setup() {
    given()
      .initializeEngine(client, engine, CommonRestrictions.builder().build())
      .and()
      .process_is_deployed("simple-process.bpmn")
    ;
  }

  @Test
  public void should_start_process_and_run_happy_path() {

  }

}
