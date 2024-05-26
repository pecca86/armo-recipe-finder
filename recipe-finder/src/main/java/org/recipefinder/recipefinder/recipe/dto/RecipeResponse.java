package org.recipefinder.recipefinder.recipe.dto;

public record RecipeResponse(int StatusCode, String message, RecipeDTO payload) {
}
