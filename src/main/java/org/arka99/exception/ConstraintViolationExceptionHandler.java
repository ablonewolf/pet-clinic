package org.arka99.exception;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.validation.exceptions.ConstraintExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import org.arka99.model.dto.response.ErrorResponse;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Singleton
@Replaces(ConstraintExceptionHandler.class)
public class ConstraintViolationExceptionHandler implements ExceptionHandler<ConstraintViolationException,
    HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .sorted()
            .reduce((left, right) -> left + "; " + right)
            .orElse("Validation failed.");

        return HttpResponse.badRequest(new ErrorResponse(HttpStatus.BAD_REQUEST.getCode(), message));
    }
}
