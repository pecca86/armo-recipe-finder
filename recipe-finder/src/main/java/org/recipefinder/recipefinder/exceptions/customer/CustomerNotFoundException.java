package org.recipefinder.recipefinder.exceptions.customer;

import org.recipefinder.recipefinder.exceptions.ApiRequestException;

public class CustomerNotFoundException extends ApiRequestException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}

