package com.turing.google_oauth.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            BadRequestException.class,
            EntityNotFoundException.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus httpStatus = (responseStatus != null) ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse(httpStatus, ex.getMessage());
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        BindingResult bindingResult = ex.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
                .map(this::resolveFieldError)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }

    private String resolveFieldError(FieldError fieldError) {
        return String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage());
    }
}