package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.ServiceTaskDeliveryStrategy.SUBSCRIPTION
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskCompletionStrategy.JOB
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskCompletionStrategy.TASKLIST
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskDeliveryStrategy.SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskDeliveryStrategy.SUBSCRIPTION_REFRESHING
import dev.bpmcrafters.processengineapi.adapter.c8.task.SubscribingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8TaskListClientUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeExternalServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.subscription.C8TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.client.ZeebeClient
import mu.KLogging
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.support.GenericApplicationContext
import java.util.function.Supplier


/**
 * Factory for creating the named C8 adapter API implementations.
 */
class C8EnginesBeanDefinitionPostProcessor(
  private val engines: Map<String, C8AdapterProperties.C8EngineConfiguration>,
  private val subscriptionRepository: SubscriptionRepository,
  private val taskListClient: CamundaTaskListClient?
) : ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

  companion object : KLogging()

  private lateinit var applicationContext: GenericApplicationContext

  override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {

    if (this::applicationContext.isInitialized) {
      val primary = engines.size == 1
      val zeebeClient = requireNotNull(applicationContext.getBean("zeebeClient", ZeebeClient::class.java)) { "ZeebeClient is a mandatory requirement" }
      logger.debug { "Creating C8 adapter components for engines: ${engines.keys.joinToString(", ")}" }

      engines.forEach { (name, engineConfigurationProperties) ->
        /*
         * Incoming API implementations
         */
        when (engineConfigurationProperties.serviceTasks.deliveryStrategy) {
          SUBSCRIPTION -> {
            val delivery = SubscribingServiceTaskDelivery(
              subscriptionRepository = subscriptionRepository,
              zeebeClient = zeebeClient,
              workerId = engineConfigurationProperties.serviceTasks.workerId
            )

            registry.registerBeanDefinition<SubscribingServiceTaskDelivery>("$name-service-task-delivery", primary, SubscribingServiceTaskDelivery::subscribe.name) { delivery }

//          applicationContext.registerBean(
//            "$name-service-task-delivery",
//            SubscribingServiceTaskDelivery::class.java,
//            delivery,
//            BeanDefinitionCustomizer { beanDefinition ->
//              beanDefinition.initMethodName = SubscribingServiceTaskDelivery::subscribe.name
//            }
//          )
          }
        }

        val subscribingUserTaskDelivery: SubscribingUserTaskDelivery? = when (engineConfigurationProperties.userTasks.deliveryStrategy) {
          SCHEDULED -> {
            val delivery = PullUserTaskDelivery(
              subscriptionRepository = subscriptionRepository,
              taskListClient = requireNotNull(taskListClient) { "TaskListClient is mandatory if using ${SCHEDULED.name} user task delivery." }
            )

            registry.registerBeanDefinition<PullUserTaskDelivery>("$name-user-task-delivery", primary) { delivery }


//          applicationContext.registerBean(
//            "$name-user-task-delivery",
//            PullUserTaskDelivery::class.java,
//            delivery
//          )
            null
          }

          SUBSCRIPTION_REFRESHING -> {
            val delivery = SubscribingRefreshingUserTaskDelivery(
              subscriptionRepository = subscriptionRepository,
              zeebeClient = zeebeClient,
              workerId = engineConfigurationProperties.serviceTasks.workerId,
              userTaskLockTimeoutMs = engineConfigurationProperties.userTasks.fixedRateRefreshRate
            )

            registry.registerBeanDefinition<SubscribingRefreshingUserTaskDelivery>("$name-user-task-delivery", primary, SubscribingRefreshingUserTaskDelivery::subscribe.name) { delivery }


//          applicationContext.registerBean(
//            "$name-user-task-delivery",
//            SubscribingRefreshingUserTaskDelivery::class.java,
//            delivery,
//            BeanDefinitionCustomizer { beanDefinition ->
//              beanDefinition.initMethodName = SubscribingRefreshingUserTaskDelivery::subscribe.name
//            }
//          )
            delivery
          }
        }

        /*
       * Outgoing API implementations
       */
//      applicationContext.registerBean(
//        "$name-service-task-completion",
//        ExternalTaskCompletionApi::class.java,
//        C8ZeebeExternalServiceTaskCompletionApiImpl(
//          zeebeClient = zeebeClient,
//          subscriptionRepository = subscriptionRepository
//        )
//      )

        registry.registerBeanDefinition<ExternalTaskCompletionApi>("$name-service-task-completion-api", primary) {
          C8ZeebeExternalServiceTaskCompletionApiImpl(
            zeebeClient = zeebeClient,
            subscriptionRepository = subscriptionRepository
          )
        }

        val userTaskCompletionApi = when (engineConfigurationProperties.userTasks.completionStrategy) {
          JOB -> {
            C8ZeebeUserTaskCompletionApiImpl(
              zeebeClient = zeebeClient,
              subscriptionRepository = subscriptionRepository
            )
          }

          TASKLIST -> {
            C8TaskListClientUserTaskCompletionApiImpl(
              taskListClient = requireNotNull(taskListClient) { "TaskListClient is mandatory if using ${TASKLIST.name} user task completion." },
              subscriptionRepository = subscriptionRepository
            )
          }
        }
        registry.registerBeanDefinition<UserTaskCompletionApi>("$name-user-task-completion-api", primary) { userTaskCompletionApi }


        // applicationContext.registerBean("$name-user-task-completion", UserTaskCompletionApi::class.java, userTaskCompletionApi)


//      applicationContext.registerBean("$name-start-process-api", StartProcessApi::class.java, StartProcessApiImpl(zeebeClient = zeebeClient))
//      applicationContext.registerBean("$name-correlation-api", CorrelationApi::class.java, CorrelationApiImpl(zeebeClient = zeebeClient))
//      applicationContext.registerBean("$name-signal-api", SignalApi::class.java, SignalApiImpl(zeebeClient = zeebeClient))
//      applicationContext.registerBean("$name-deployment-api", DeploymentApi::class.java, DeploymentApiImpl(zeebeClient = zeebeClient))

        registry.registerBeanDefinition<StartProcessApi>("$name-start-process-api", primary) { StartProcessApiImpl(zeebeClient = zeebeClient) }
        registry.registerBeanDefinition<CorrelationApi>("$name-correlation-api", primary) { CorrelationApiImpl(zeebeClient = zeebeClient) }
        registry.registerBeanDefinition<SignalApi>("$name-signal-api", primary) { SignalApiImpl(zeebeClient = zeebeClient) }
        registry.registerBeanDefinition<DeploymentApi>("$name-deployment-api", primary) { DeploymentApiImpl(zeebeClient = zeebeClient) }


//      applicationContext.registerBean(
//        "$name-task-subscription-api", TaskSubscriptionApi::class.java, C8TaskSubscriptionApiImpl(
//          subscriptionRepository = subscriptionRepository,
//          subscribingUserTaskDelivery = subscribingUserTaskDelivery,
//        )
//      )

        registry
          .registerBeanDefinition<TaskSubscriptionApi>("$name-task-subscription-api", primary) {
            C8TaskSubscriptionApiImpl(
              subscriptionRepository = subscriptionRepository,
              subscribingUserTaskDelivery = subscribingUserTaskDelivery,
            )
          }
      }
    }
  }

  override fun setApplicationContext(applicationContext: ApplicationContext) {
    require(applicationContext is GenericApplicationContext) { "This factory can operate on generic context only, but instance of ${applicationContext.javaClass.name} was passed." }
    this.applicationContext = applicationContext
  }


  private inline fun <reified T : Any> BeanDefinitionRegistry.registerBeanDefinition(
    beanName: String,
    markPrimary: Boolean,
    initMethodName: String? = null,
    instanceSupplier: Supplier<T>
  ) {
    this.registerBeanDefinition(
      beanName,
      BeanDefinitionBuilder
        .genericBeanDefinition(T::class.java, instanceSupplier)
        .let {
          if (initMethodName != null) {
            it.setInitMethodName(initMethodName)
          } else {
            it
          }
        }
        .beanDefinition
    )
  }

}
