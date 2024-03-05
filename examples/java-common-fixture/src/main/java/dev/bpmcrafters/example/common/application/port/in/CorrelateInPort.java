package dev.bpmcrafters.example.common.application.port.in;

import java.util.concurrent.Future;

public interface CorrelateInPort {

  /**
   * Correlates message.
   * @param correlationKey correlation key.
   * @param value string value.
   * @return nothing.
   */
  Future<Void> correlateMessage(String correlationKey, String value);
}
