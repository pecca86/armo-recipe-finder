package org.recipefinder.recipefinder.exceptions.customer;

import org.recipefinder.recipefinder.exceptions.ApiRequestException;

public class CustomerAlreadyExistsException extends ApiRequestException {
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}

