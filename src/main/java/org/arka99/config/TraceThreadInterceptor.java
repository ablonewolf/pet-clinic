package org.arka99.config;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@InterceptorBean(TraceThread.class)
public class TraceThreadInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(TraceThreadInterceptor.class);

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String layer = context.stringValue(TraceThread.class)
            .filter(value -> !value.isBlank())
            .orElse("component");
        String method = context.getDeclaringType().getSimpleName() + "." + context.getMethodName();

        log.info("[{}] entering {} on thread={}", layer, method, Thread.currentThread().getName());

        try {
            Object result = context.proceed();
            log.info("[{}] leaving {} on thread={}", layer, method, Thread.currentThread().getName());
            return result;
        } catch (RuntimeException e) {
            log.error("[{}] failed {} on thread={}", layer, method, Thread.currentThread().getName(), e);
            throw e;
        }
    }
}
