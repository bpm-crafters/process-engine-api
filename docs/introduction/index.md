Instead of learning and using of vendor-specific APIs to build your process applications, you should focus on delivering business value. Construction
of scalable, robust and future-proof applications is easy, if system components are designed to fulfill clear goals and have clear responsibilities.
This kind of architecture is called [Clean architecture](clean-architecture.md), and we recommend to follow it in general situations. In doing so, technology is hidden inside adapters and the business and domain logic stays technology-agnostic and independent.

Our experience with different process engines led us to an idea to create a vendor-independent API, abstracting main capabilities of a process engine.
In doing so, we followed several ideas:

* The API must be asynchronous.
* The user should not make assumptions about the implementations of the API, to stay portable if those are exchanged.
* The API offers the minimal set of capabilities instead of support all possible vendor-specific product features, which have nothing to do with process orchestration.
* Different aspects of process engine integration are solved using different independent APIs.
* The API is command based (you pass immutable commands indicating your intent).
* For interaction with your software, the API uses subscriptions.
* The vendor adapters implementing APIs are drop-ins to your application classpath and your application should not depend on it (rather you can configure those independently).

Having all this in mind, we provide a Process Engine API with several adapters, which are ready to use in your next process application. In particular,
you can use Process Engine API in your application and configure the adapter to use it with the following process engines / operation modes:

* Camunda Platform 7 Embedded (Spring Boot)
* Camunda Platform 7 Remote (connected via REST)
* Camunda Platform 8 Self Managed
* Camunda Platform 8 SaaS / Cloud
