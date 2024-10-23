package com.example.msvcprimefinder.exception;

import com.example.msvcprimefinder.response.FindPrimesErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String GENERAL_EXCEPTION_ERROR_MESSAGE = "An unexpected Error has occurred";
    private static final String MAX_LONG_VALUE = "9223372036854775807";

    @ExceptionHandler(InvalidNumberException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleInvalidNumberException(InvalidNumberException ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMsg = String.format("Invalid value '%s' for parameter '%s'. Please provide a valid number less than or equal to: %s", ex.getValue(), ex.getName(), MAX_LONG_VALUE);
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(errorMsg, HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FindPrimesErrorResponse> handleGeneralException(Exception ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(GENERAL_EXCEPTION_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
