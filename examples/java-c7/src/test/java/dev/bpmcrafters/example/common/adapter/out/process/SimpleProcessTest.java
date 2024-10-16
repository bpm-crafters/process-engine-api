package dev.bpmcrafters.example.common.adapter.out.process;

import com.tngtech.jgiven.junit5.DualScenarioTest;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import org.junit.jupiter.api.BeforeEach;


public class SimpleProcessTest extends DualScenarioTest<SimpleProcessStages.ActionStage, SimpleProcessStages.AssertStage> {

  // TODO -> coverage extension

  @BeforeEach
  public void setup() {
    given()
      .initializeEngine(null, CommonRestrictions.builder().build())
      .and()
      .process_is_deployed("simple-process.bpmn")
    ;
  }

  // @Test
  public void should_start_process_and_run_happy_path() {

  }

}
