package org.arka99.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.arka99.model.dto.response.ErrorResponse;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Singleton
public class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException,
    HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, IllegalArgumentException exception) {
        return HttpResponse.badRequest(new ErrorResponse(HttpStatus.BAD_REQUEST.getCode(), exception.getMessage()));
    }
}
