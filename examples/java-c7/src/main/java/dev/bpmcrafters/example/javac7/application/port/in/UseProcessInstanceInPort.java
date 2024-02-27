package dev.bpmcrafters.example.javac7.application.port.in;

import java.util.concurrent.Future;

public interface UseProcessInstanceInPort {
  /**
   * Starts a new process instance.
   * @param value string value.
   * @param intValue int value.
   * @return process instance id.
   */
  Future<String> startNew(String value, Integer intValue);

  /**
   * Correlates message.
   * @param processInstanceId process instance id.
   * @param value string value.
   * @return nothing.
   */
  Future<Void> correlateMessage(String processInstanceId, String value);

  /**
   * Delivers signal.
   * @param value string value.
   * @return nothing.
   */
  Future<Void> deliverSignal(String value);
}
