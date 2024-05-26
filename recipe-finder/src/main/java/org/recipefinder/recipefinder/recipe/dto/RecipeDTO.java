package org.recipefinder.recipefinder.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecipeDTO(Long id, String description, @JsonProperty("is_vegan") Boolean isVegan, @JsonProperty("num_servings") Integer numServings, List<String> ingredients) {
}
