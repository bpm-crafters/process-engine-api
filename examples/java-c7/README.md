# Java Example to demonstrate usage of process API

This example is a test that we can invoke API defined in Kotlin from Java. It utilizes the API directly.

## Features in the example

There are some features in the C7 adapter already. In addition, there are some features in the example: 

- AbstractSynchronousTaskHandler to complete external tasks in a synchronous way
- In-Memory user task pool for retrieving infos about open user tasks (FIXME: currently there is no way to tell that the user task is already completed!)

## Process

![Service Task Process](src/main/resources/service-tasks.png)


## How to run

- Build with Maven
- Start `JavaCamunda7ExampleApplication`
- Open http://localhost:8080/swagger-ui/index.html
- Start process
- Wait, wait, wait, check the logs, wait...
- Copy the resulting retrieve the user tasks
- Complete the user task with id
- Wait, wait, wait, check the logs, wait...
- Correlate message by providing the process instance id
- Hint: don't hurry, the error of correlation is not implemented yet (if you try it before both tasks are executed)
