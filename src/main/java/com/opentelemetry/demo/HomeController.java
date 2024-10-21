package com.opentelemetry.demo;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final Tracer tracer;

    @Autowired
    public HomeController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/hello")
    public String hello() {
        Span span = tracer.spanBuilder("example-span").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.addEvent("handling GET /hello");
            return "Hello, World!";
        } finally {
            span.end();
        }
    }
}