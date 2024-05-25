package org.recipefinder.recipefinder.recipe.dto;

import org.recipefinder.recipefinder.recipe.Recipe;

public record RecipeResponse(int StatusCode, String message, Recipe payload) {
}
