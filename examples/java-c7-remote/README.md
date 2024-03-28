# Java Example to demonstrate usage of process API

This example is a test that we can invoke API defined in Kotlin from Java. It utilizes the API directly.

## Features in the example

There are some features in the C7 adapter already. In addition, there are some features in the example: 

- AbstractSynchronousTaskHandler to complete external tasks in a synchronous way
- In-Memory user task pool for retrieving infos about open user tasks

## Process

![Service Task Process](src/main/resources/simple-process.png)


## How to run

- Build with Maven
- Start `JavaCamunda7ExampleApplication`
- Open http://localhost:8080/swagger-ui/index.html
- Start process
- Wait, wait, wait, check the logs, wait...
- Copy the resulting retrieved user task id
- Complete the user task with id
- Wait, wait, wait, check the logs, wait...
- Correlate message by providing the process instance id
- Hint: don't hurry, the error of correlation is not implemented yet (if you try it before both tasks are executed)

## How to run using IntelliJ test script
- Build with Maven
- Start `JavaCamunda7ExampleApplication`
- Run `simple-process-demo.http` script
- Analyze the results
- Run `simple-process-demo-failed-user.http` script
- Analyze the results
