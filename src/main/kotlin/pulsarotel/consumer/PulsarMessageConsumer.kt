package pulsarotel.consumer

import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.MessageListener
import org.apache.pulsar.client.api.PulsarClientException
import org.springframework.stereotype.Component

@Component
class PulsarMessageConsumer : MessageListener<String> {

    private val logger = KotlinLogging.logger {}

    override fun received(
            consumer: Consumer<String?>,
            message: Message<String?>
    ) {
        try {
            val msg = readMessage(message)
            logger.info { "received $msg" }
            consumer.acknowledge(message)
        } catch (e: PulsarClientException) {
            consumer.negativeAcknowledge(message)
            logger.error { e.message }
        }
    }

    @WithSpan
    private fun readMessage(message: Message<String?>) =
            message.value
}
