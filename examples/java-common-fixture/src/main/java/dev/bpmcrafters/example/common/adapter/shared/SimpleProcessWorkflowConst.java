package dev.bpmcrafters.example.common.adapter.shared;

public class SimpleProcessWorkflowConst {

  public static final String BPMN = "simple-process.bpmn";
  public static final String KEY = "simple-process-1";

  public enum Elements {
    ;
    // Start events
    public static final String START_EVENT = "started";

    // service tasks
    public static final String SERVICE_TASK_DO_ACTION_1 = "service-do-action1";
    public static final String SERVICE_TASK_DO_ACTION_2 = "service-do-action2";

    // user tasks
    public static final String USER_TASK_PERFORM_TASK = "user-perform-task";

    // events
    public static final String EVENT_RECEIVED_MESSAGE = "received_message";
    public static final String TIMER_PASSED = "timer_passed";

    // signals
    public static final String EVENT_SIGNAL_OCCURRED = "received_signal";

    // End events
    public static final String END_EVENT = "finished";
    public static final String END_EVENT_ABNORMALLY = "finished_abnormally";
  }

  public enum Expressions {
    ;
    public static final String MESSAGE_1 = "message1";

    public static final String ERROR_ACTION_ERROR = "action_error";

    public static final String SIGNAL_1 = "signal1";

    public static final String JOB_TYPE_EXECUTE_ACTION_EXTERNAL = "execute-action-external";
    public static final String JOB_TYPE_SEND_MESSAGE_EXTERNAL = "send-message-external";
  }

}
