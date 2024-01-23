# Java Example to demonstrate usage of process API

This example is a test that we can invoke API defined in Kotlin from Java. It utilizes the API directly.

## Process

![Service Task Process](src/main/resources/service-tasks.png)


## How to run

- Build with Maven
- Start `JavaCamunda7ExampleApplication`
- Open http://localhost:8080/swagger-ui/index.html
- Start process
- Copy the resulting process instance id from response
- Wait, wait, wait, check the logs, wait...
- Correlate message by providing the process instance id
- Hint: don't hurry, the error of correlation is not implemented yet (if you try it before both tasks are executed)
