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
    public static final String EVENT_RECEIVE_MESSAGE = "receive_message";

    // signals
    public static final String EVENT_SIGNAL_OCCURRED = "receive_signal";

    // End events
    public static final String END_EVENT = "ended";
    public static final String END_EVENT_ABNORMALLY = "ended_abnormally";
  }

  public enum Messages {
    ;
    public static final String MESSAGE_1 = "message1";
  }

  public enum Errors {
    ;

    public static final String MESSAGE_ACTION_ERROR = "action_error";
  }

  public enum Signals {
    ;
    public static final String SIGNAL_1 = "signal1";
  }

  public enum Topics {
    ;
    public static final String EXECUTE_ACTION_EXTERNAL = "execute-action-external";
    public static final String SEND_MESSAGE_EXTERNAL = "send-message-external";
  }

}
