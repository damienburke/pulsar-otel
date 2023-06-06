# pulsar-otel

This project provides a basic Spring Boot app that contains an Apache Pulsar producer and consumer, as well as a REST
API. The REST API invokes the producer, which produces a messages to a topic, which in turn is processed by the
consumer. The app is instrument with Otel, but not configured to export data. Here we just rely on looking at the logs
to see what telemetry data is generated.

## basic

All the above exists for a "basic" pulsar producer / consumer and all works as expected. I can see the following spans:

* send
* receive
* process

(as instrumented by pulsar), plus my manual instrumentation. ALL spans have the same trace ID. This can be seen by
looking at the logs and viewing the `io.opentelemetry.exporter.logging.LoggingSpanExporter` entries (as opposed to
checking a configured OTEL backend).

## Flow

I then have a very similar setup of a pulsar producer and consumer, but the consumer is using `kotlinx.coroutines.flow`
APIs to process the messages. The spans are not created as expected. As before, I do see a:

* send
* receive
, and they do have same trace ID. I dont see a process Span, and all the consumer spans have a different traceId.

# To start up the app and a pulsar broker:

```shell 
docker compose down --volumes

# build app container (for arm? add `-Djib.platform-arch=arm64`)
mvn package -DskipTests jib:dockerBuild 

# run the app container and pulsar broker in your local docker 
docker compose up
```

```shell 
./invokeBasic
```

``` 
invokeBasic Output (see traceId 253118a83007e9302287f222a1b5ff9c propagated)

[pulsar-client-io-1-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'persistent://public/default/otel-topic send' : 253118a83007e9302287f222a1b5ff9c 371ffa82eb123f90 PRODUCER [tracer: io.opentelemetry.pulsar-2.8:1.26.0-alpha] AttributesMap{data={messaging.system=pulsar, net.peer.name=pulsar, messaging.destination.kind=topic, net.peer.port=6650, messaging.message.id=13:0:-1, messaging.destination.name=persistent://public/default/otel-topic, messaging.message.payload_size_bytes=21, thread.id=36, thread.name=http-nio-8080-exec-1}, capacity=128, totalAddedValues=9}
[http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'PulsarController.produceBasicMessage' : 253118a83007e9302287f222a1b5ff9c 510bfc19f203b035 INTERNAL [tracer: io.opentelemetry.spring-webmvc-6.0:1.26.0-alpha] AttributesMap{data={thread.id=36, thread.name=http-nio-8080-exec-1}, capacity=128, totalAddedValues=2}
[http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'POST /restyBasic' : 253118a83007e9302287f222a1b5ff9c 1cfbabfbc58dc885 SERVER [tracer: io.opentelemetry.tomcat-10.0:1.26.0-alpha] AttributesMap{data={http.status_code=200, user_agent.original=curl/7.88.1, net.host.name=localhost, http.response_content_length=0, http.target=/restyBasic, net.sock.peer.addr=172.24.0.1, net.host.port=9083, net.sock.peer.port=54882, net.sock.host.port=8080, thread.id=36, thread.name=http-nio-8080-exec-1, http.route=/restyBasic, net.sock.host.addr=172.24.0.3, net.protocol.name=http, net.protocol.version=1.1, http.scheme=http, http.method=POST}, capacity=128, totalAddedValues=17}
[pulsar-client-internal-4-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'persistent://public/default/otel-topic receive' : 253118a83007e9302287f222a1b5ff9c 796be66e2e84c4a7 CONSUMER [tracer: io.opentelemetry.pulsar-2.8:1.26.0-alpha] AttributesMap{data={messaging.system=pulsar, net.peer.name=pulsar, messaging.destination.kind=topic, net.peer.port=6650, messaging.message.id=13:0:-1, messaging.destination.name=persistent://public/default/otel-topic, messaging.message.payload_size_bytes=21, messaging.operation=receive, thread.id=34, thread.name=pulsar-client-internal-4-1}, capacity=128, totalAddedValues=10}
[pulsar-external-listener-3-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'PulsarMessageConsumer.readMessage' : 253118a83007e9302287f222a1b5ff9c 589b8f1b49c3b646 INTERNAL [tracer: io.opentelemetry.opentelemetry-instrumentation-annotations-1.16:1.26.0-alpha] AttributesMap{data={code.namespace=pulsarotel.consumer.PulsarMessageConsumer, code.function=readMessage, thread.id=51, thread.name=pulsar-external-listener-3-1}, capacity=128, totalAddedValues=4}
[pulsar-external-listener-3-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'persistent://public/default/otel-topic process' : 253118a83007e9302287f222a1b5ff9c 1a40cc0708636e6a INTERNAL [tracer: io.opentelemetry.pulsar-2.8:1.26.0-alpha] AttributesMap{data={messaging.system=pulsar, messaging.destination.kind=topic, messaging.message.id=13:0:-1, messaging.destination.name=persistent://public/default/otel-topic, messaging.message.payload_size_bytes=21, messaging.operation=process, thread.id=51, thread.name=pulsar-external-listener-3-1}, capacity=128, totalAddedValues=8}
```

```shell 
./invokeFlow
```

```
invokeFlow Output (see traceId 6645949db3a49b55e8ef411ed6864276 propagated up as far as receive span only)

[pulsar-client-io-1-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'persistent://public/default/otel-topic2 send' : 6645949db3a49b55e8ef411ed6864276 72b502fdb76bd05a PRODUCER [tracer: io.opentelemetry.pulsar-2.8:1.26.0-alpha] AttributesMap{data={messaging.system=pulsar, net.peer.name=pulsar, messaging.destination.kind=topic, net.peer.port=6650, messaging.message.id=10:0:-1, messaging.destination.name=persistent://public/default/otel-topic2, messaging.message.payload_size_bytes=24, thread.id=38, thread.name=http-nio-8080-exec-3}, capacity=128, totalAddedValues=9}
[http-nio-8080-exec-3] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'PulsarController.produceFlowMessage' : 6645949db3a49b55e8ef411ed6864276 5d8b5f6b3d25da5a INTERNAL [tracer: io.opentelemetry.spring-webmvc-6.0:1.26.0-alpha] AttributesMap{data={thread.id=38, thread.name=http-nio-8080-exec-3}, capacity=128, totalAddedValues=2}
[http-nio-8080-exec-3] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'POST /restyFlow' : 6645949db3a49b55e8ef411ed6864276 108367229e740d32 SERVER [tracer: io.opentelemetry.tomcat-10.0:1.26.0-alpha] AttributesMap{data={http.status_code=200, user_agent.original=curl/7.88.1, net.host.name=localhost, http.response_content_length=0, http.target=/restyFlow, net.sock.peer.addr=172.24.0.1, net.host.port=9083, net.sock.peer.port=55706, net.sock.host.port=8080, thread.id=38, thread.name=http-nio-8080-exec-3, http.route=/restyFlow, net.sock.host.addr=172.24.0.3, net.protocol.name=http, net.protocol.version=1.1, http.scheme=http, http.method=POST}, capacity=128, totalAddedValues=17}
[pulsar-client-internal-4-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'persistent://public/default/otel-topic2 receive' : 6645949db3a49b55e8ef411ed6864276 3588e3900b1f428f CONSUMER [tracer: io.opentelemetry.pulsar-2.8:1.26.0-alpha] AttributesMap{data={messaging.system=pulsar, net.peer.name=pulsar, messaging.destination.kind=topic, net.peer.port=6650, messaging.message.id=10:0:-1, messaging.destination.name=persistent://public/default/otel-topic2, messaging.message.payload_size_bytes=24, messaging.operation=receive, thread.id=34, thread.name=pulsar-client-internal-4-1}, capacity=128, totalAddedValues=10}
[DefaultDispatcher-worker-4] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'ConsumerFlow.processFlowMessage' : 91574d3c28e5911e06e749b8b90e7f93 40c3dbb3f80474e7 INTERNAL [tracer: io.opentelemetry.opentelemetry-instrumentation-annotations-1.16:1.26.0-alpha] AttributesMap{data={code.namespace=pulsarotel.consumer.ConsumerFlow, code.function=processFlowMessage, thread.id=30, thread.name=DefaultDispatcher-worker-4}, capacity=128, totalAddedValues=4}
``` 
invokeFlow Output (see traceId a AND b)

# pulsar-otel
# pulsar-otel
# pulsar-otel
# pulsar-otel
# pulsar-otel
