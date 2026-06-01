package org.arka99.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.arka99.model.dto.response.ErrorResponse;
import org.jspecify.annotations.NullMarked;

import java.util.NoSuchElementException;

@NullMarked
@Singleton
public class ResourceNotFoundExceptionHandler implements ExceptionHandler<NoSuchElementException,
    HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, NoSuchElementException exception) {
        return HttpResponse.notFound(new ErrorResponse(HttpStatus.NOT_FOUND.getCode(), exception.getMessage()));
    }
}
