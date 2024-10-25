package dev.bpmcrafters.example.common.adapter.out.process;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import dev.bpmcrafters.example.common.adapter.shared.SimpleProcessWorkflowConst.Elements;
import dev.bpmcrafters.example.common.adapter.shared.SimpleProcessWorkflowConst.Expressions;
import dev.bpmcrafters.example.common.application.port.out.UserTaskOutPort;
import dev.bpmcrafters.example.common.application.port.out.WorkflowOutPort;
import dev.bpmcrafters.processengineapi.adapter.c8.testing.AbstractC8ProcessStage;
import java.util.HashMap;

public class SimpleProcessStages {

  static class ActionStage extends AbstractC8ProcessStage<ActionStage> {

    @ProvidedScenarioState
    private WorkflowOutPort workflowOutPort;
    @ProvidedScenarioState
    private UserTaskOutPort userTaskOutPort;
    private String correlationKey;

    @Override
    public void initialize() {
      workflowOutPort = new WorkflowAdapter(
        startProcessApi,
        signalApi,
        correlationApi,
        deploymentApi
      );

      userTaskOutPort = new UserTaskAdapter(
        userTaskSupport,
        userTaskCompletionApi
      );
    }

    @As("simple process started with value $value and intValue $intValue")
    public ActionStage simple_process_started(@Quoted String value, @Quoted Integer intValue) {
      String instanceId = workflowOutPort.startSimpleProcess(value, intValue);
      assertThat(instanceId).isNotNull();
      this.correlationKey = value;
      return self();
    }

    @As("service task execute action completed with $value")
    public ActionStage service_execute_action_is_completed(@Quoted String value) {
      HashMap<String, String> payload = new HashMap<>();
      payload.put("action1", value);

      return external_task_exists(Expressions.JOB_TYPE_EXECUTE_ACTION_EXTERNAL, Elements.SERVICE_TASK_DO_ACTION_1)
        .and()
        .external_task_is_completed(Expressions.JOB_TYPE_EXECUTE_ACTION_EXTERNAL, payload);
    }

    @As("service task execute action completed with error")
    public ActionStage service_execute_action_is_completed_with_error() {
      return external_task_exists(Expressions.JOB_TYPE_EXECUTE_ACTION_EXTERNAL, Elements.SERVICE_TASK_DO_ACTION_1)
        .and()
        .external_task_is_completed_with_error(Expressions.JOB_TYPE_EXECUTE_ACTION_EXTERNAL, Expressions.ERROR_ACTION_ERROR, new HashMap<String, String>());
    }

    @As("user task perform task completed with $value")
    public ActionStage user_task_perform_task_is_completed(String value) {
      process_waits_in(Elements.USER_TASK_PERFORM_TASK);
      userTaskOutPort.complete(task().getTaskId(), value);
      return self();
    }

    @As("user task perform task is timed out")
    public ActionStage user_task_perform_task_is_timed_out() {
      process_waits_in(Elements.USER_TASK_PERFORM_TASK);
      timer_passes(61);
      return self();
    }

    @As("user task perform task is complete with an error with $value")
    public ActionStage user_task_perform_task_is_completed_with_error(String value) {
      process_waits_in(Elements.USER_TASK_PERFORM_TASK);
      userTaskOutPort.completeWithError(task().getTaskId(), value);
      return self();
    }

    @As("service task message send completed")
    public ActionStage service_send_email_is_completed() {
      return external_task_exists(Expressions.JOB_TYPE_SEND_MESSAGE_EXTERNAL, Elements.SERVICE_TASK_DO_ACTION_2)
        .and()
        .external_task_is_completed(Expressions.JOB_TYPE_SEND_MESSAGE_EXTERNAL, new HashMap<String, String>());
    }

    @As("message received with $value")
    public ActionStage message_received(String value) {
      process_waits_in_element(Elements.EVENT_RECEIVED_MESSAGE);
      workflowOutPort.correlateMessage(correlationKey, value);
      return self();
    }

    @As("signal occurred")
    public ActionStage signal_occurred() {
      process_waits_in_element(Elements.EVENT_SIGNAL_OCCURRED);
      workflowOutPort.deliverSignal(correlationKey);
      return self();
    }

  }

  static class AssertStage extends AbstractC8ProcessStage<AssertStage> {

  }
}
