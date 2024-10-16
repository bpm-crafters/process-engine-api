package dev.bpmcrafters.example.common.adapter.out.process;

import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import dev.bpmcrafters.example.common.application.port.out.WorkflowOutPort;
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.testing.AbstractC7EmbeddedStage;

public class SimpleProcessStages {
  static class ActionStage extends AbstractC7EmbeddedStage<ActionStage> {

    @ProvidedScenarioState
    private WorkflowOutPort workflowOutPort;

    @Override
    public void initialize() {
      workflowOutPort = new WorkflowAdapter(
        startProcessApi,
        signalApi,
        correlationApi,
        deploymentApi
      );
    }

    public ActionStage process_is_started(String value, Integer intValue) {
      workflowOutPort.startSimpleProcess(value, intValue);
      return self();
    }
  }

  static class AssertStage extends AbstractC7EmbeddedStage<AssertStage> {

  }
}
