package com.example.msvcprimefinder.exception;

import com.example.msvcprimefinder.response.FindPrimesErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String GENERAL_EXCEPTION_ERROR_MESSAGE = "An unexpected Error has occurred";
    private static final String MAX_LONG_VALUE = "9223372036854775807 (max long value)";
    private static final String CONCURRENCY_ERROR = "Failed to compute prime numbers due to a processing error";
    private static final String OUT_OF_MEMORY_ERROR = "Out of memory. Please try a smaller limit.";

    @ExceptionHandler(FindPrimesArgException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleFindPrimesArgException(FindPrimesArgException ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        logger.warn("Primes Arg Exception: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        logger.warn("Constraint Violation Exception: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConcurrentSieveException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleConcurrentSieveException(ConcurrentSieveException ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(CONCURRENCY_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
        logger.error("Error in Concurrent Sieve calculation: " + ex.getMessage() + " with cause: " + ex.getCause());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var type = Objects.requireNonNull(ex.getRequiredType(), "Required type should never be null here");
        String errorMessage;
        switch (ex.getName()) {
            case "algo" -> errorMessage = "Invalid value for '" + ex.getName() + "'. Allowed values are: " + Arrays.toString(type.getEnumConstants());
            case "limit" -> errorMessage = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'. Please provide a valid limit less than or equal to: " + MAX_LONG_VALUE;
            default -> errorMessage = "Invalid value for '" + ex.getName() + "'. Allowed values are: [true, false]";
        }
        logger.warn("Method argument mismatch: {}", ex.getMessage());
        return new ResponseEntity<>(new FindPrimesErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity<FindPrimesErrorResponse> handleOutOfMemoryError(OutOfMemoryError ex) {
        logger.error(OUT_OF_MEMORY_ERROR);
        return new ResponseEntity<>(new FindPrimesErrorResponse(OUT_OF_MEMORY_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<FindPrimesErrorResponse> handleAllErrors(Throwable ex) {
        logger.error(GENERAL_EXCEPTION_ERROR_MESSAGE);
        return new ResponseEntity<>(new FindPrimesErrorResponse(GENERAL_EXCEPTION_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FindPrimesErrorResponse> handleGeneralException(Exception ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(GENERAL_EXCEPTION_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        logger.error("Unhandled exception thrown: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
