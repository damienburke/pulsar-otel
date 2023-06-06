package pulsarotel.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.pulsar.client.api.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class PulsarConfig {

    @Value("\${app.pulsar.client.url}")
    private lateinit var pulsarClientUrl: String

    @Value("\${app.pulsar.topic}")
    private lateinit var topic_BASIC: String

    @Value("\${app.pulsar.topic2}")
    private lateinit var topic_FLOW: String

    @Bean
    open fun objectMapper() = ObjectMapper()

    @Bean
    open fun pulsarClient(): PulsarClient {
        return PulsarClient.builder().serviceUrl(pulsarClientUrl).build()
    }

    @Bean
    open fun producer_BASIC(client: PulsarClient): Producer<*> {
        return client.newProducer(Schema.STRING)
            .topic(topic_BASIC)
            .create()
    }

    @Bean
    open fun consumer_BASIC(client: PulsarClient, pulsarMessageConsumer: MessageListener<String>): Consumer<*> {
        return client.newConsumer(Schema.STRING)
            .topic(topic_BASIC)
            .subscriptionName("subscription_BASIC")
            .subscriptionType(SubscriptionType.Shared)
            .messageListener(pulsarMessageConsumer)
            .subscribe()
    }

    @Bean
    open fun producer_FLOW(client: PulsarClient): Producer<*> {
        return client.newProducer(Schema.STRING)
            .topic(topic_FLOW)
            .create()
    }

    @Bean(name= ["consumer_FLOW"])
    open fun consumer_FLOW(client: PulsarClient): Consumer<*> {
        return client.newConsumer(Schema.STRING)
            .topic(topic_FLOW)
            .subscriptionName("subscription_FLOW")
            .subscriptionType(SubscriptionType.Shared)
            .subscribe()
    }
}
