package org.arka99.config;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
@ServerFilter("/**")
public class ThreadTraceServerFilter {

    private static final Logger log = LoggerFactory.getLogger(ThreadTraceServerFilter.class);

    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        // This filter traces the request at Micronaut's HTTP layer.
        //
        // Why it exists:
        // - Netty transport logging shows when traffic reaches the server socket/pipeline.
        // - Controller/service/repository tracing shows business-layer execution.
        // - This filter fills the gap between those two and shows the thread Micronaut uses
        //   once the request is inside the framework's HTTP processing flow.
        //
        // This is useful when comparing:
        // - the Netty event-loop thread that first received the request
        // - the Micronaut-managed thread that actually handles controller execution
        //
        // In other words, this is the first application-level checkpoint after transport handling.
        log.info(
            "[http] request {} {} on thread={}",
            request.getMethodName(),
            request.getPath(),
            Thread.currentThread().getName()
        );
    }

    @ResponseFilter
    public void onResponse(HttpRequest<?> request, MutableHttpResponse<?> response) {
        // This runs after the controller/service flow has produced a response but before the
        // response leaves Micronaut's HTTP layer.
        //
        // Why it exists:
        // - It shows the application-side thread that finished request processing.
        // - It can then be compared with the lower-level Netty outbound write logs to see
        //   whether the response is written back on a different thread.
        //
        // This makes it easier to understand the boundary between:
        // - Micronaut response handling
        // - Netty transport write-back to the client
        log.info(
            "[http] response {} {} status={} on thread={}",
            request.getMethodName(),
            request.getPath(),
            response.getStatus().getCode(),
            Thread.currentThread().getName()
        );
    }
}
