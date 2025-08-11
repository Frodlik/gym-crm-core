package com.gym.crm.exception;

import com.gym.crm.openapi.model.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static com.gym.crm.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.exception.ApiError.INVALID_REQUEST_ERROR;
import static com.gym.crm.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.exception.ApiError.SERVER_ERROR;
import static com.gym.crm.exception.ApiError.TOO_MANY_REQUESTS_ERROR;
import static com.gym.crm.exception.ApiError.VALIDATION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class ErrorHandlerTest {
    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Invalid trainee username format is wrong",
            "Invalid trainer username provided",
            "Invalid training type specified",
            "Invalid date format in request",
            "INVALID TRAINEE USERNAME something"
    })
    void handleCoreServiceException_shouldReturnInvalidRequestError_whenPrefixMatches(String message) {
        CoreServiceException ex = new CoreServiceException(message);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleCoreServiceException(ex);

        assertNotNull(actual.getBody());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
        assertEquals(String.valueOf(INVALID_REQUEST_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertTrue(actual.getBody().getErrorMessage().contains(INVALID_REQUEST_ERROR.getMessage()));
    }

    @Test
    void handleCoreServiceException_shouldReturnServerError_whenPrefixDoesNotMatch() {
        String message = "Some unknown service exception";
        CoreServiceException ex = new CoreServiceException(message);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleCoreServiceException(ex);

        assertNotNull(actual.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(String.valueOf(SERVER_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertEquals(SERVER_ERROR.getMessage(), actual.getBody().getErrorMessage());
    }

    @Test
    void handleDaoException_shouldReturnDatabaseError() {
        TransactionHandlerException ex = new TransactionHandlerException("DB failure");

        ResponseEntity<ErrorResponse> actual = errorHandler.handleDaoException(ex);

        assertNotNull(actual.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(String.valueOf(DATABASE_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertEquals(DATABASE_ERROR.getMessage(), actual.getBody().getErrorMessage());
    }

    @Test
    void handleEntityNotFoundExceptions_shouldReturnNotFoundErrorWithMessage() {
        String message = "Trainee with ID 5 not found";
        EntityNotFoundException ex = new EntityNotFoundException(message);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleEntityNotFoundExceptions(ex);

        assertNotNull(actual.getBody());
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(String.valueOf(NOT_FOUND_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertTrue(actual.getBody().getErrorMessage().contains(NOT_FOUND_ERROR.getMessage()));
        assertTrue(actual.getBody().getErrorMessage().contains(message));
    }

    @Test
    void handleValidationExceptions_shouldReturnCleanedMessage() {
        String message = "Validation error: create.arg0.field: Field must not be blank";
        ConstraintViolationException ex = new ConstraintViolationException(message, null);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleValidationExceptions(ex);

        assertNotNull(actual.getBody());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
        assertEquals(String.valueOf(VALIDATION_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertTrue(actual.getBody().getErrorMessage().contains("Field must not be blank"));
    }

    @Test
    void handleUserNotAuthenticatedExceptions_shouldReturnAuthenticationError() {
        NotAuthenticatedException ex = new NotAuthenticatedException("No token");

        ResponseEntity<ErrorResponse> actual = errorHandler.handleUserNotAuthenticatedExceptions(ex);

        assertNotNull(actual.getBody());
        assertEquals(UNAUTHORIZED, actual.getStatusCode());
        assertEquals(String.valueOf(AUTHENTICATION_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertEquals(AUTHENTICATION_ERROR.getMessage(), actual.getBody().getErrorMessage());
    }

    @Test
    void handleUnhandledExceptions_shouldReturnServerError() {
        RuntimeException ex = new RuntimeException("Unknown runtime issue");

        ResponseEntity<ErrorResponse> actual = errorHandler.handleUnhandledExceptions(ex);

        assertNotNull(actual.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(String.valueOf(SERVER_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertEquals(SERVER_ERROR.getMessage(), actual.getBody().getErrorMessage());
    }

    @Test
    void handleCoreServiceException_shouldHandleNullMessage() {
        CoreServiceException ex = new CoreServiceException(null);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleCoreServiceException(ex);

        assertNotNull(actual.getBody());
        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(String.valueOf(SERVER_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertEquals(SERVER_ERROR.getMessage(), actual.getBody().getErrorMessage());
    }

    @Test
    void handleUserBlockedException_shouldReturnTooManyRequestsError() {
        LocalDateTime blockExpiration = LocalDateTime.now().plusMinutes(5);
        UserBlockedException ex = new UserBlockedException("user.test", blockExpiration);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleUserBlockedException(ex);

        assertNotNull(actual.getBody());
        assertEquals(TOO_MANY_REQUESTS, actual.getStatusCode());
        assertEquals(String.valueOf(TOO_MANY_REQUESTS_ERROR.getCode()), actual.getBody().getErrorCode().toString());
        assertEquals(TOO_MANY_REQUESTS_ERROR.getMessage(), actual.getBody().getErrorMessage());
    }

    @Test
    void handleUserBlockedException_shouldHandleNullBlockExpiration() {
        UserBlockedException ex = new UserBlockedException("test.user", null);

        ResponseEntity<ErrorResponse> actual = errorHandler.handleUserBlockedException(ex);

        assertNotNull(actual.getBody());
        assertEquals(TOO_MANY_REQUESTS, actual.getStatusCode());
        assertTrue(actual.getBody().getErrorMessage().contains("Too many requests"));
    }
}

