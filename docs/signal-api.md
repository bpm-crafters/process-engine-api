---
title: Signal API
---

The Signal API provides functionality to send signals to running process instances.
It is intended to be used in outbound adapters of the port/adapter architecture, in order
to control the process engine from your application.


```java
class SignalUseCase {

  private final SignalApi signalApi;

  /**
   * Send `signal1`signal to the process. 
   * @param payloadValue value to set as process variable on successful correlation.
   */
  void senSignal1(String payloadValue) {
    signalApi.sendSignal(
      new SendSignalCmd(
        "signal1",
        () -> Map.of(
          "signal-delivered-value", payloadValue
        ),
        Map.of()
      )
    ).get();
  }
}

```
