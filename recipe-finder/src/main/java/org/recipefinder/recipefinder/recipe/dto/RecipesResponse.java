package org.recipefinder.recipefinder.recipe.dto;

import org.recipefinder.recipefinder.recipe.Recipe;

import java.util.List;

public record RecipesResponse(int StatusCode, String message, List<Recipe> payload) {
}
