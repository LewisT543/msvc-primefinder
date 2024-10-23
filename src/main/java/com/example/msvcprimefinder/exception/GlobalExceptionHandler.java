package com.example.msvcprimefinder.exception;

import com.example.msvcprimefinder.response.FindPrimesErrorResponse;
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

    @ExceptionHandler(FindPrimesArgException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleFindPrimesArgException(FindPrimesArgException ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // I did not use @Validators as I wanted the flexibility to throw/catch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FindPrimesErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var type = Objects.requireNonNull(ex.getRequiredType(), "Required type should never be null here"); // Silence compiler for isEnum() check
        String errorMessage;
        if (type.isEnum()) {
            // an invalid value for PrimeAlgorithms enum argument
            errorMessage = "Invalid value for '" + ex.getName() + "'. Allowed values are: " + Arrays.toString(type.getEnumConstants());
        } else {
            errorMessage = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'. Please provide a valid limit less than or equal to: " + MAX_LONG_VALUE;
        }
        logger.error("Method argument mismatch: {}", ex.getMessage());
        return new ResponseEntity<>(new FindPrimesErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FindPrimesErrorResponse> handleGeneralException(Exception ex) {
        FindPrimesErrorResponse errorResponse = new FindPrimesErrorResponse(GENERAL_EXCEPTION_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
