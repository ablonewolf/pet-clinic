package org.arka99.config;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.scheduling.executor.ThreadSelection;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiredArgsConstructor
public class ThreadSelectionLogger implements ApplicationEventListener<StartupEvent> {

    private final HttpServerConfiguration httpServerConfiguration;
    private static final Logger log = LoggerFactory.getLogger(ThreadSelectionLogger.class.getName());

    @Override
    public void onApplicationEvent(@NonNull StartupEvent event) {
        ThreadSelection selection = httpServerConfiguration.getThreadSelection();
        log.info("Micronaut server thread-selection is: {}", selection);
    }
}
