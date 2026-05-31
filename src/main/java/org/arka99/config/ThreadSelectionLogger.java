package org.arka99.config;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.scheduling.executor.ThreadSelection;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
@Singleton
@RequiredArgsConstructor
public class ThreadSelectionLogger implements ApplicationEventListener<StartupEvent> {

    private final HttpServerConfiguration httpServerConfiguration;
    private static final Logger log = LoggerFactory.getLogger(ThreadSelectionLogger.class.getName());

    @Override
    public void onApplicationEvent(StartupEvent event) {
        // The rest of the tracing setup logs *actual* execution threads at runtime.
        // This startup log records Micronaut's configured thread-selection strategy once,
        // so those runtime logs can be interpreted correctly later.
        //
        // Example:
        // - MANUAL means user code is responsible for offloading blocking work.
        // - AUTO means Micronaut may move synchronous controller work off the Netty event loop.
        // - IO / BLOCKING force request execution onto a configured executor.
        //
        // Without this log, thread changes seen later in controller/service/repository logs
        // are harder to explain just by looking at a single request trace.
        ThreadSelection selection = httpServerConfiguration.getThreadSelection();
        log.info("Micronaut server thread-selection is: {}", selection);
    }
}
