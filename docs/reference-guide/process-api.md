---
title: Process API
---

The Process API provides functionality, required to control the lifecycle of the processes. It allows to start new process instances.
It is intended to be used in outbound adapters of the port/adapter architecture in order to control the process engine
from your application.

There are two ways to start processes: 
- by providing a process definition key
- by providing a start message

In both cases, you might provide a process payload passed to the started process instance. 

Here is an example of usage:

```java

@Component
@RequiredArgsConstructor
public class ProcessStarter {

  private final StartProcessApi startProcessApi;

  @SneakyThrows
  public void startByProcessDefinition(Order order) {
    startProcessApi.startProcess(
      new StartProcessByDefinitionCmd(
        "MyExampleProcessKey",
        () -> Map.of("order", order)
      )
    ).get();
  }


  @SneakyThrows
  public void startByMessage(Order order) {
    startProcessApi.startProcess(
      new StartProcessByMessageCmd(
        "Msg_OrderReceived",
        () -> Map.of("order", order)
      )
    ).get();
  }
  
}


```
