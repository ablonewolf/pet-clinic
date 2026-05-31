package org.arka99.config;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
@Singleton
@InterceptorBean(TraceThread.class)
public class TraceThreadInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(TraceThreadInterceptor.class);

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        // Read the optional layer label from @TraceThread.
        // If no label is supplied, keep a generic fallback so the interceptor still produces
        // useful logs when applied ad-hoc to a method or class.
        String layer = context.stringValue(TraceThread.class)
            .filter(value -> !value.isBlank())
            .orElse("component");
        String method = context.getDeclaringType().getSimpleName() + "." + context.getMethodName();

        // Log before proceeding so we can see the thread that entered the method.
        // This is the key signal for spotting thread handoff between controller, service,
        // and repository layers inside a single request.
        log.info("[{}] entering {} on thread={}", layer, method, Thread.currentThread().getName());

        try {
            Object result = context.proceed();

            // Log again after the invocation completes. In synchronous code this will usually be
            // the same thread, but keeping both logs makes it obvious if an execution boundary or
            // framework behavior changes that assumption later.
            log.info("[{}] leaving {} on thread={}", layer, method, Thread.currentThread().getName());
            return result;
        } catch (RuntimeException e) {
            // Keep the failure log at the same tracing point so exceptions still show which layer
            // and which thread were active when execution failed.
            log.error("[{}] failed {} on thread={}", layer, method, Thread.currentThread().getName(), e);
            throw e;
        }
    }
}
