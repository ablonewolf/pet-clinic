package org.arka99.config;

import io.micronaut.aop.Around;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NullMarked;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NullMarked
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Around
@Singleton
public @interface TraceThread {

    // Optional logical label included in the log message.
    //
    // Typical values in this project are:
    // - controller
    // - service
    // - repository
    //
    // The label is not functionally required by Micronaut AOP, but it makes the logs much easier
    // to scan when a single request passes through multiple traced layers.
    String value() default "";
}
