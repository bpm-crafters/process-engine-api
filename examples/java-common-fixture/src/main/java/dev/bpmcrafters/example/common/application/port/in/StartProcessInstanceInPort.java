package dev.bpmcrafters.example.common.application.port.in;

import java.util.concurrent.Future;

public interface StartProcessInstanceInPort {
  /**
   * Starts a new process instance.
   * @param value string value.
   * @param intValue int value.
   * @return process instance id.
   */
  Future<String> startNew(String value, Integer intValue);

}
