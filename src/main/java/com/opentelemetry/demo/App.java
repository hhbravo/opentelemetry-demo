package com.opentelemetry.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import io.opentelemetry.api.common.Attributes;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App {

  private static final String SERVICE_NAME = "opentelemetry";
  private static OpenTelemetry openTelemetry;

  public static void main(String[] args) {
    initializeOpenTelemetry();
    SpringApplication.run(App.class, args);
  }


  private static void initializeOpenTelemetry() {
    // Create a resource with service name for identifying the service in traces
    Resource resource = Resource.getDefault()
        .merge(Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, SERVICE_NAME)
        ));

    // Set up the OpenTelemetry exporter (OTLP in this case)
    OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
    .setEndpoint("http://otel-collector:4343")   // Cambia localhost a otel-collector
        .build();

    // Create a tracer provider with the span processor and exporter
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .setResource(resource)
            .build();

    openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .buildAndRegisterGlobal();
}


  @RestController
  class HomeController {
    private Tracer tracer = GlobalOpenTelemetry.getTracer("example-tracer");

    @GetMapping("/hello")
    public String hello() {
      Span span = tracer.spanBuilder("example-span")
      .setSpanKind(SpanKind.SERVER)
      .startSpan();
      
      try(Scope scope = span.makeCurrent()) {
        return "Hola";
      } finally {
        span.end();
      }
    }
  }
}
