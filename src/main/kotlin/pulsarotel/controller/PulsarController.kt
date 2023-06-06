package pulsarotel.controller

import io.opentelemetry.api.trace.Span
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pulsarotel.dto.MyDTO
import pulsarotel.producer.PulsarProducer

@RestController
class PulsarController(private val pulsarProducer: PulsarProducer) {

    private val logger = KotlinLogging.logger {}

    @PostMapping(value = ["/restyBasic"])
    fun produceBasicMessage(): ResponseEntity<String> {
        logger.info { "PRODUCER" }
        logger.info { "Span ID ${Span.current().spanContext.spanId}" }
        logger.info { "Trace ID ${Span.current().spanContext.traceId}" }
        pulsarProducer.produceMessageBasic(MyDTO("itsbasicyo"))
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping(value = ["/restyFlow"])
    fun produceFlowMessage(): ResponseEntity<String> {
        logger.info { "PRODUCER" }
        logger.info { "Span ID ${Span.current().spanContext.spanId}" }
        logger.info { "Trace ID ${Span.current().spanContext.traceId}" }
        pulsarProducer.produceMessageFlow(MyDTO("gowiththeflow"))
        return ResponseEntity(HttpStatus.OK)
    }
}
