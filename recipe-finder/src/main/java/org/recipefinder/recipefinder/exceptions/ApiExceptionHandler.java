package org.recipefinder.recipefinder.exceptions;

import jakarta.transaction.TransactionalException;
import org.recipefinder.recipefinder.exceptions.customer.CustomerAlreadyExistsException;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.recipefinder.recipefinder.exceptions.recipe.RecipeAccessException;
import org.recipefinder.recipefinder.exceptions.recipe.RecipeNotFoundException;
import org.recipefinder.recipefinder.exceptions.recipe.RecipeValidationExceptionResponse;
import org.slf4j.Logger;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.ZonedDateTime;
import java.util.Optional;

@ControllerAdvice // This annotation makes this class a global exception handler
public class ApiExceptionHandler {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<?> handleException(Exception e) {

        if (e instanceof DataIntegrityViolationException exception) {
            RecipeValidationExceptionResponse response = new RecipeValidationExceptionResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    Optional.ofNullable(exception.getRootCause())
                            .map(Throwable::getMessage)
                            .orElse("Please provide all required fields")
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (e instanceof AuthenticationException exp) {
            ApiException exception = new ApiException(
                    exp.getMessage(),
                    HttpStatus.UNAUTHORIZED,
                    ZonedDateTime.now()
            );
            LOGGER.error("AuthenticationException: {}", exp.getMessage(), exp);
            return new ResponseEntity<>(exception, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(new ApiException("The operation failed!", HttpStatus.INTERNAL_SERVER_ERROR, ZonedDateTime.now()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {TransactionSystemException.class})
    public ResponseEntity<Object> handleTransactionalException(TransactionSystemException e) {
        String error = e.getRootCause().getMessage();
        if (error.contains("Email should be valid")) {
            error = "Email should be valid";
        }
        ApiException exception = new ApiException(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now()
        );
        LOGGER.error("TransactionalException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        RecipeValidationExceptionResponse response = new RecipeValidationExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                Optional.ofNullable(e.getRootCause()).map(Throwable::getMessage).orElse("Please provide all required fields")
        );
        LOGGER.error("DataIntegrityViolationException: {}", e.getMessage(), e);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        RecipeValidationExceptionResponse response = new RecipeValidationExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                Optional.ofNullable(e.getBindingResult().getFieldError())
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .orElse("Please provide all required fields")
        );
        LOGGER.error("MethodArgumentNotValidException: {}", e.getMessage(), e);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.NOT_FOUND,
                ZonedDateTime.now()
        );
        LOGGER.error("UsernameNotFoundException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {org.recipefinder.recipefinder.exceptions.AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(org.recipefinder.recipefinder.exceptions.AuthenticationException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now()
        );
        LOGGER.error("AuthenticationException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // parse only the values that are inside the []
        String message = "Allowed ingredient units: " + e.getMessage().substring(e.getMessage().indexOf('[') + 1, e.getMessage().indexOf(']'));
        ApiException exception = new ApiException(
                message,
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        LOGGER.error("HttpMessageNotReadableException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        LOGGER.error("ApiRequestException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        LOGGER.error("ApiRequestException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {CustomerNotFoundException.class})
    public ResponseEntity<Object> handleCustomerNotFoundException(CustomerNotFoundException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.NOT_FOUND,
                ZonedDateTime.now()
        );
        LOGGER.error("CustomerNotFoundException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {CustomerAlreadyExistsException.class})
    public ResponseEntity<Object> handleCustomerAlreadyExistsException(CustomerAlreadyExistsException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.CONFLICT,
                ZonedDateTime.now()
        );
        LOGGER.error("CustomerAlreadyExistsException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {RecipeNotFoundException.class})
    public ResponseEntity<Object> handleRecipeNotFoundException(RecipeNotFoundException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.NOT_FOUND,
                ZonedDateTime.now()
        );
        LOGGER.error("RecipeNotFoundException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {RecipeAccessException.class})
    public ResponseEntity<Object> handleRecipeAccountAccessException(RecipeAccessException e) {
        ApiException exception = new ApiException(
                e.getMessage(),
                HttpStatus.FORBIDDEN,
                ZonedDateTime.now()
        );
        LOGGER.error("RecipeAccessException: {}", e.getMessage(), e);
        return new ResponseEntity<>(exception, HttpStatus.FORBIDDEN);
    }

}

