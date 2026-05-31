package org.arka99.config;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerFilter("/**")
public class ThreadTraceServerFilter {

    private static final Logger log = LoggerFactory.getLogger(ThreadTraceServerFilter.class);

    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        log.info(
            "[http] request {} {} on thread={}",
            request.getMethodName(),
            request.getPath(),
            Thread.currentThread().getName()
        );
    }

    @ResponseFilter
    public void onResponse(HttpRequest<?> request, MutableHttpResponse<?> response) {
        log.info(
            "[http] response {} {} status={} on thread={}",
            request.getMethodName(),
            request.getPath(),
            response.getStatus().getCode(),
            Thread.currentThread().getName()
        );
    }
}
