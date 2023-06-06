package pulsarotel.consumer

import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.instrumentation.annotations.WithSpan
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.Closeable

@Component
class ConsumerFlow(
    @Qualifier("consumer_FLOW") private val consumer: Consumer<String>
) : Closeable {

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val logger = KotlinLogging.logger {}

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Default + Context.current().asContextElement())

    override fun close() {
        job.cancel()
    }

    @PostConstruct
    fun start() = consumer.consumerAsFlow()
        .onCompletion { cause ->
            if (cause != null)
                consumer.closeAsync().await()
        }
        .flowOn(dispatcher)
        .onEach { logger.info { processFlowMessage(String(it.data)) } }
        .catch {
            consumer.closeAsync().await()
        }
        .launchIn(scope)

    @WithSpan
    private fun processFlowMessage(data: String) {

        logger.info { "I MADE IT $String($data)" }
        logger.info { "Span ID ${Span.current().spanContext.spanId}" }
        logger.info { "Trace ID ${Span.current().spanContext.traceId}" }
    }
}


fun <T> Consumer<T>.consumerAsFlow(): Flow<Message<T>> {
    val flow = flow {
        while (true) {
            val message = receiveAsync().await()
            emit(message)
        }
    }.catch { _ ->

    }
    return flow
}