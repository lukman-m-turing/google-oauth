package com.turing.google_oauth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private HttpStatus status;
    private String message;
}
