package dev.bpmcrafters.example.common.application.port.in;

import java.util.concurrent.Future;

public interface SignalInPort {
  /**
   * Delivers signal.
   * @param value string value.
   * @return nothing.
   */
  Future<Void> deliverSignal(String value);
}
