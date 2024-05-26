package org.recipefinder.recipefinder.exceptions;

public class AuthenticationException extends RuntimeException{
    public AuthenticationException(String message) {
        super(message);
    }
}
