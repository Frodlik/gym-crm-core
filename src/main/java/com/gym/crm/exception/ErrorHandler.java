package com.gym.crm.exception;

import com.gym.crm.openapi.model.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gym.crm.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.exception.ApiError.INVALID_REQUEST_ERROR;
import static com.gym.crm.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.exception.ApiError.SERVER_ERROR;
import static com.gym.crm.exception.ApiError.TOO_MANY_REQUESTS_ERROR;
import static com.gym.crm.exception.ApiError.VALIDATION_ERROR;

@ControllerAdvice
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    private static final Set<String> BAD_REQUEST_PREFIXES = Set.of(
            "Invalid trainee username",
            "Invalid trainer username",
            "Invalid training type",
            "Invalid date format",
            "Username and password are required"
    );

    @ExceptionHandler(CoreServiceException.class)
    public ResponseEntity<ErrorResponse> handleCoreServiceException(CoreServiceException ex) {
        logger.error("ServiceException: {}", ex.getMessage(), ex);
        ApiError error = resolveError(ex);

        return buildErrorResponse(error);
    }

    @ExceptionHandler(TransactionHandlerException.class)
    public ResponseEntity<ErrorResponse> handleDaoException(TransactionHandlerException ex) {
        logger.error("Database Exception: {}", ex.getMessage(), ex);

        return buildErrorResponse(DATABASE_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundExceptions(Exception ex) {
        logger.error("Entity not found Exception: {}", ex.getMessage(), ex);

        return buildErrorResponse(NOT_FOUND_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex) {
        logger.error("Validation Exception: {}", ex.getMessage(), ex);

        String cleanedMessage = extractConstraintMessage(ex.getMessage());

        return buildErrorResponse(VALIDATION_ERROR, cleanedMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnhandledExceptions(Exception ex) {
        logger.error("Unhandled Exception: {}", ex.getMessage(), ex);

        return buildErrorResponse(SERVER_ERROR);
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthenticatedExceptions(Exception ex) {
        logger.error("Authentication Exception: {}", ex.getMessage(), ex);

        return buildErrorResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponse> handleUserBlockedException(Exception ex){
        logger.error("User Blocked Exception: {}", ex.getMessage());

        return buildErrorResponse(TOO_MANY_REQUESTS_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ApiError apiError) {
        return buildErrorResponse(apiError, null);

    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ApiError apiError, String message) {
        message = StringUtils.isBlank(message) ? "" : message;

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(apiError.getCode());
        errorResponse.setErrorMessage(apiError.getMessage() + message);

        return new ResponseEntity<>(errorResponse, apiError.getHttpStatus());
    }

    private ApiError resolveError(CoreServiceException ex) {
        String normalizedMessage = StringUtils.isBlank(ex.getMessage()) ? "" : ex.getMessage().toLowerCase();

        return BAD_REQUEST_PREFIXES.stream()
                .map(String::toLowerCase)
                .filter(normalizedMessage::startsWith)
                .findFirst()
                .map(suffix -> INVALID_REQUEST_ERROR)
                .orElse(SERVER_ERROR);
    }

    private String extractConstraintMessage(String rawMessage) {
        Pattern pattern = Pattern.compile(":\\s([^:]+)$");
        Matcher matcher = pattern.matcher(rawMessage);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return rawMessage;
    }
}
