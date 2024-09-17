## Why should I use this?

Our experience with different process engines led us to an idea to create a vendor-independent API abstracting main capabilities of a process engine.
In doing so, we followed several ideas:

* The API must be asynchronous.
* The user should not make assumptions about the implementations of the API to stay portable if those are exchanged.
* The API offers the minimal set of capabilities instead of support all possible vendor specific product features, which have nothing to do with process orchestration.
* Different aspects of process engine integration are solved using different independent APIs.
* The API is command based (you pass immutable commands indicating your intent).
* For interaction with your software, the API uses subscriptions.
* The vendor-adapters implementing APIs are drop-ins to your application classpath and your application should not depend on it (rather you can configure those independently)

Having all this in mind, we created a Process Engine API with several adapters which are ready to use in your next process application. In particular, 
you can use Process Engine API in your application and configure the adapter to use it with the following process engines / operation modes:

* Camunda Platform 7 Embedded (Spring Boot)
* Camunda Platform 7 Remote (connected via REST)
* Camunda Platform 8 Self Managed
* Camunda Platform 8 SaaS / Cloud

In doing so, you might start your application using one engine / mode and easily migrate to the other later on, by changing a dependency and providing some 
additional configuration.

Sounds interesting for you? Try it out, and provide us some feedback...

## How to start?

We provide documentation for different people and different tasks. A good starting point is the
[Introduction](./introduction). You might want to look at [Reference Guide](./reference-guide).

## Get in touch

If you are missing a feature, have a question regarding usage or deployment, you should definitely get in touch
with us. Here is a link to the open issues of the project:

[![Github Issues](https://img.shields.io/github/issues/bpm-crafters/process-engine-api)](https://github.com/bpm-crafters/process-engine-api/issues)

You might want to help with their implementation or file some new or just contribute your opinion in the discussions. At any rate, any 
participation or feedback is highly welcome.
