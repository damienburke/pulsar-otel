package pulsarotel.producer

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.api.trace.Span
import mu.KotlinLogging
import org.apache.pulsar.client.api.Producer
import org.apache.pulsar.client.api.PulsarClientException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import pulsarotel.dto.MyDTO

@Service
class PulsarProducer(
    @Qualifier("producer_BASIC") private val pulsarProducerBasic: Producer<String>,
    @Qualifier("producer_FLOW") private val pulsarProducerFlow: Producer<String>,
    private val objectMapper: ObjectMapper
) {

    private val logger = KotlinLogging.logger {}

    fun produceMessageBasic(message: MyDTO) {
        try {
            logger.info { "PRODUCER" }
            logger.info { "Span ID ${Span.current().spanContext.spanId}" }
            logger.info { "Trace ID ${Span.current().spanContext.traceId}" }
            val value = objectMapper.writeValueAsString(message)
            pulsarProducerBasic.send(value)

        } catch (e: PulsarClientException) {
            logger.error { e.message }
        }
    }

    fun produceMessageFlow(message: MyDTO) {
        try {
            logger.info { "PRODUCER" }
            logger.info { "Span ID ${Span.current().spanContext.spanId}" }
            logger.info { "Trace ID ${Span.current().spanContext.traceId}" }
            val value = objectMapper.writeValueAsString(message)
            pulsarProducerFlow.send(value)

        } catch (e: PulsarClientException) {
            logger.error { e.message }
        }
    }
}
