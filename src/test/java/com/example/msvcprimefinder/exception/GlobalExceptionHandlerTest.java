package com.example.msvcprimefinder.exception;

import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void handleFindPrimesArgException() throws Exception {
        FindPrimesArgException exception = new FindPrimesArgException("Invalid limit");

        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleFindPrimesArgException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid limit", response.getBody().message());
    }

    @Test
    public void handleConcurrentSieveException() throws Exception {
        ConcurrentSieveException exception = new ConcurrentSieveException("Error", new Error("Cause"));

        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleConcurrentSieveException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to compute prime numbers due to a processing error", response.getBody().message());
    }

    @Test
    public void handleMethodArgumentTypeMismatchException_Limit() throws Exception {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("limit");
        when(exception.getValue()).thenReturn("NaN");
        Class<?> expectedClass = Long.class;
        when(exception.getRequiredType()).thenReturn((Class) expectedClass);

        // Simulate the exception handling
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Invalid value 'NaN' for parameter 'limit'"));
    }

    @Test
    public void handleMethodArgumentTypeMismatchException_Enum() throws Exception {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("algo");
        when(exception.getValue()).thenReturn("SIEV");
        when(exception.getRequiredType()).thenReturn((Class) PrimeAlgorithmNames.class);

        // Simulate the exception handling
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for 'algo'. Allowed values are: " + Arrays.toString(PrimeAlgorithmNames.values()), response.getBody().message());
    }

    @Test
    public void handleThrowable_OOM() throws Exception {
        Throwable exception = new OutOfMemoryError("Memory limit exceeded");

        // Simulate the exception handling
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleAllErrors(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Out of memory. Please try a smaller limit.", response.getBody().message());
    }

    @Test
    public void handleThrowable_Other() throws Exception {
        Throwable exception = new StackOverflowError("Stack overflow");

        // Simulate the exception handling
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleAllErrors(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected Error has occurred", response.getBody().message());
    }

    @Test
    public void handleGeneralException() throws Exception {
        Exception exception = new Exception("General error");

        // Simulate the exception handling
        ResponseEntity<FindPrimesErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected Error has occurred", response.getBody().message());
    }
}
