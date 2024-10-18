package dev.bpmcrafters.example.common.adapter.out.process;

import com.tngtech.jgiven.junit5.DualScenarioTest;
import dev.bpmcrafters.example.common.adapter.shared.SimpleProcessWorkflowConst;
import dev.bpmcrafters.example.common.adapter.shared.SimpleProcessWorkflowConst.Elements;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SimpleProcessTest extends DualScenarioTest<SimpleProcessStages.ActionStage, SimpleProcessStages.AssertStage> {

  @RegisterExtension
  static ProcessEngineCoverageExtension extension = ProcessEngineCoverageExtension.builder().build();

  @BeforeEach
  public void setup() {
    given()
      .initializeEngine(extension.getProcessEngine(), CommonRestrictions.builder().build())
      .and()
      .process_is_deployed(SimpleProcessWorkflowConst.BPMN);
  }

  @Test
  public void should_start_process_and_run_happy_path() {
    given()
      .simple_process_started("test", 123);

    when()
      .service_execute_action_is_completed("value1")
      .and()
      .process_waits_in(Elements.USER_TASK_PERFORM_TASK)
      .and()
      .task_is_assigned_to_user("kermit")
      .and()
      .user_task_perform_task_is_completed("user-task-value")
      .and()
      .service_send_email_is_completed()
      .and()
      .message_received("message-value");

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_EVENT);
  }

  @Test
  public void should_start_process_and_handle_service_task_error() {
    given()
      .simple_process_started("test", 123);

    when()
      .service_execute_action_is_completed_with_error()
      .and()
      .signal_occurred();

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_EVENT_ABNORMALLY);
  }

  @Test
  public void should_start_process_and_handle_user_task_timeout() {
    given()
      .simple_process_started("test", 123);

    when()
      .service_execute_action_is_completed("value1")
      .and()
      .user_task_perform_task_is_timed_out()
      .and()
      .signal_occurred();

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_EVENT_ABNORMALLY);
  }

  @Test
  public void should_start_process_and_handle_user_task_error() {
    given()
      .simple_process_started("test", 123);

    when()
      .service_execute_action_is_completed("value1")
      .and()
      .user_task_perform_task_is_completed_with_error("task error value")
      .and()
      .signal_occurred();

    then()
      .process_is_finished()
      .and()
      .process_has_passed(Elements.END_EVENT_ABNORMALLY);
  }

}
