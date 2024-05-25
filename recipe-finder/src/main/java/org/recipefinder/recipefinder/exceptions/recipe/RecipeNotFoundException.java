package org.recipefinder.recipefinder.exceptions.recipe;

import org.recipefinder.recipefinder.exceptions.ApiRequestException;

public class RecipeNotFoundException extends ApiRequestException {
    public RecipeNotFoundException(String message) {
        super(message);
    }
}
