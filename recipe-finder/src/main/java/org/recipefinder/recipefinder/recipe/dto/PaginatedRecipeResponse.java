package org.recipefinder.recipefinder.recipe.dto;

import org.springframework.data.domain.Page;

public record PaginatedRecipeResponse(Page<RecipeDTO> recipes) {
}
