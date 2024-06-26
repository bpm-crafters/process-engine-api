If you want to try out `process-engine-api` library in your project, there are two steps you need to perform.

First, add the library to your project classpath. In maven it is just adding the following dependency into your 
project's `pom.xml`:

```xml 
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api</groupId>
    <artifactId>process-engine-api</artifactId>
    <version>${process-engine-api.version}</version>
  </dependency>
```

This dependency provides you with the most important classes required for implementation of your system using
a process engine. Here is an example how a user task can be completed:

```java

import dev.bpmcrafters.processengineapi.task.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompleteUserTaskUseCase implements CompleteUserTaskInPort {

  private final UserTaskCompletionApi taskCompletionApi;

  @Override
  public void completeUserTaskWithSomeValue(String taskId, String value) {
    taskCompletionApi.completeTask(
      new CompleteTaskCmd(
        taskId,
        () -> Map.of("some-user-value", value)
      )
    ).get();
  }

}

```

As you can see, the code above doesn't contain any engine-specific code, but rather uses only the `process-engine-api`.
This means that the resulting code is portable and the decision about used engine doesn't influence the implementation
of you application logic.

The second step depends on your target architecture and used process engine. Please refer to the one of the following
configurations:

## Camunda Platform 7 running as embedded engine

If you start with a Camunda Platform 7 operated in an embedded engine mode, by for example using the Camunda Spring Boot Starter,
the following configuration is applicable for you. 

First of all add the corresponding adapter to your project's classpath:

```xml 
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api</groupId>
    <artifactId>process-engine-api-adapter-camunda-platform-c7-embedded-spring-boot-starter</artifactId>
    <version>${process-engine-api.version}</version>
  </dependency>
```

and finally add the following configuration to your configuration properties. Here is a version for `application.yaml`:

```yaml 
dev:
  bpm-crafters:
    process-api:
      adapter:
        c7:
          embedded:
            external-service-tasks:
              delivery-strategy: embedded_job
              worker-id: embedded-worker
              lock-time-in-seconds: 10
            user-tasks:
              delivery-strategy: embedded_job

```

## Camunda Platform 7 running as remote engine

If you start with a Camunda Platform 7 operated remotely, the following configuration is applicable for you.

First of all add the corresponding adapter to your project's classpath. In order to connect to remote engine,
you will need to use some client. Assuming you are using Camunda Hub extension [camunda-platform-7-rest-client-spring-boot](https://github.com/camunda-community-hub/camunda-platform-7-rest-client-spring-boot),
you will also need to add some additional libraries. Here is the result:

```xml

<dependendcies>
  <!-- the correct adapter -->
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api</groupId>
    <artifactId>process-engine-api-adapter-camunda-platform-c7-remote-spring-boot-starter</artifactId>
    <version>${process-engine-api.version}</version>
  </dependency>
  <!-- rest client library --> 
  <dependency>
    <groupId>org.camunda.community.rest</groupId>
    <artifactId>camunda-platform-7-rest-client-spring-boot-starter</artifactId>
    <version>7.21.0</version>
  </dependency>
  <!-- open feign client -->
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
  </dependency>
</dependendcies>

```

and finally add the following configuration to your configuration properties. Here is a version for `application.yaml`:

```yaml 
dev:
  bpm-crafters:
    process-api:
      adapter:
        c7:
          remote:
            external-service-tasks:
              delivery-strategy: remote_scheduled
              fixed-rate-schedule-rate: 10
              worker-id: embedded-worker
              lock-time-in-seconds: 10
            user-tasks:
              fixed-rate-schedule-rate: 10
              delivery-strategy: remote_scheduled

# to tell the client library where the engine is located provide the correct details below:
feign:
  client:
    config:
      default:
        url: "http://localhost:9090/engine-rest/"

```

## Camunda Platform 8 as SaaS

If you start with a Camunda Platform 8 operated as SaaS, the following configuration is applicable for you.

First add the corresponding adapter to your project's classpath:

```xml 
<dependencies>
    <dependency>
      <groupId>dev.bpm-crafters.process-engine-api</groupId>
      <artifactId>process-engine-api-adapter-camunda-platform-c8-spring-boot-starter</artifactId>
    </dependency>

    <!-- We need the camunda client too -->
    <dependency>
      <groupId>io.camunda.spring</groupId>
      <artifactId>spring-boot-starter-camunda</artifactId>
    </dependency>
    <dependency>
      <groupId>io.camunda.spring</groupId>
      <artifactId>java-common</artifactId>
    </dependency>
</dependencies>
```

and configure the adapter in your application properties:

```yaml

dev:
  bpm-crafters:
    process-api:
      adapter:
        c8:
          user-tasks:
            delivery-strategy: subscription_refreshing
            fixed-rate-schedule-rate: 5000 # every 5 seconds
            tasklist-url: https://${zeebe.client.cloud.region}.tasklist.camunda.io/${zeebe.client.cloud.clusterId}
            fixed-rate-refresh-rate: 5000 # every 5 seconds
          service-tasks:
            delivery-strategy: subscription
            worker-id: worker

zeebe:
  client:
    cloud:
      region: ${ZEEBE_REGION}
      clusterId: ${ZEEBE_CLUSTER_ID}
      clientId: ${ZEEBE_CLIENT_ID}
      clientSecret: ${ZEEBE_CLIENT_SECRET}

```
