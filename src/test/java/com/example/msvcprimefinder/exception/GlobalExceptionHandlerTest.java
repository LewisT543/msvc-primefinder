package com.example.msvcprimefinder.exception;

import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void handleFindPrimesArgException() {
        FindPrimesArgException exception = new FindPrimesArgException("Invalid limit");
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleFindPrimesArgException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid limit", response.getBody().message());
    }

    @Test
    public void handleConcurrentSieveException() {
        ConcurrentSieveException exception = new ConcurrentSieveException("Error", new Error("Cause"));
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleConcurrentSieveException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to compute prime numbers due to a processing error", response.getBody().message());
    }

    @Test
    public void handleConstraintViolationException_Limit() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        when(exception.getMessage()).thenReturn("findPrimes.limit: must be greater than or equal to 2");
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleConstraintViolationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("findPrimes.limit: must be greater than or equal to 2"));
    }

    @Test
    public void handleMethodArgumentTypeMismatchException_Enum() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("algo");
        when(exception.getValue()).thenReturn("SIEV");
        when(exception.getRequiredType()).thenReturn((Class) PrimeAlgorithmNames.class);
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for 'algo'. Allowed values are: " + Arrays.toString(PrimeAlgorithmNames.values()), response.getBody().message());
    }

    @Test
    public void handleMethodArgumentTypeMismatchException_Booleans() {
        List<String> booleanParams = List.of("useCache", "buildCache", "withResult");
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        booleanParams.forEach(param -> {
            when(exception.getName()).thenReturn(param);
            when(exception.getValue()).thenReturn("test");
            when(exception.getRequiredType()).thenReturn((Class) Boolean.class);
            ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "StatusCode should be BAD_REQUEST for: " + exception.getName());
            assertEquals("Invalid value for '" + exception.getName() + "'. Allowed values are: [true, false]", response.getBody().message(), "Message should be correct for: " + exception.getName());
        });
    }

    @Test
    public void handleThrowable_OOM() {
        OutOfMemoryError exception = new OutOfMemoryError("Memory limit exceeded");
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleOutOfMemoryError(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Out of memory. Please try a smaller limit.", response.getBody().message());
    }

    @Test
    public void handleThrowable_Other() {
        StackOverflowError exception = new StackOverflowError("Stack overflow");
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleAllErrors(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected Error has occurred", response.getBody().message());
    }

    @Test
    public void handleGeneralException() {
        Exception exception = new Exception("General error");
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected Error has occurred", response.getBody().message());
    }
}
