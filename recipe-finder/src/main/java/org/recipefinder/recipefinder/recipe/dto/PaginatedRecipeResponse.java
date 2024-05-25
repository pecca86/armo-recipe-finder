package org.recipefinder.recipefinder.recipe.dto;

import org.recipefinder.recipefinder.recipe.Recipe;
import org.springframework.data.domain.Page;

public record PaginatedRecipeResponse(Page<Recipe> recipes) {
}
