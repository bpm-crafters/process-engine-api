---
title: Overview
---

## Process Engine API

The `process-engine-api` provides an API to abstract from a concrete process engine implementation, with the ability
to write your application code engine-agnostic and later (re-)configure for the usage of a particular process engine.

The API consists of different parts independent of each other:

### Deployment API

The [Deployment API](deployment-api.md) allows to deploy process-related resources in a bundle into the engine.

### Process API

The [Process API](process-api.md) provides functionality, required to control the lifecycle of the processes. It allows to start new process instances.
It is intended to be used in outbound adapters of the port/adapter architecture, in order to control the process engine 
from your application.

### Correlation API

The [Correlation API](correlation-api.md) provides functionality to correlate messages with running process instances.
It is intended to be used in outbound adapters of the port/adapter architecture, in order to control 
the process engine from your application.

### Signal API

The [Signal API](signal-api.md) provides functionality to send signals to running process instances.
It is intended to be used in outbound adapters of the port/adapter architecture, in order 
to control the process engine from your application.

### Task Subscription API

The [Task Subscription API](task-subscription-api.md) allows for subscribing for different tasks. The process-engine-api adapter implementation
then takes care of the delivery of the tasks matching the intended subscription.

### Service Task Completion API

The [Service Task Completion API](service-task-completion-api.md) provides functionality to deal with service tasks. The task handlers can be registered 
and get invoked when tasks appear in the process engine. Since the Task Subscription API allows asynchronous processing, 
we provide a special API to complete tasks.

### User Task Completion API

The [User Task Completion API](user-task-completion-api.md) provides functionality to deal with user tasks. Since the Task Subscription API allows asynchronous processing,
we provide a special API to complete tasks. 

## Other Helpful Components

### User Task Support

The [User Task Support](user-task-support.md) is a small component simplifying use cases related to user tasks. It can be hooked up into Task Subscription API
and receive and store the received user tasks. If later you want to access a task delivered in the past, its `TaskInformation` and payload are available in the
User Task Support component.


