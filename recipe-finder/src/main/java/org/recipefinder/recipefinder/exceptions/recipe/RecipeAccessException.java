package org.recipefinder.recipefinder.exceptions.recipe;

import org.recipefinder.recipefinder.exceptions.ApiRequestException;

public class RecipeAccessException extends ApiRequestException {
    public RecipeAccessException(String message) {
        super(message);
    }
}
