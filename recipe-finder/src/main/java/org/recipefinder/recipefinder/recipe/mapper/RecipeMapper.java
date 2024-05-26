package org.recipefinder.recipefinder.recipe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import org.recipefinder.recipefinder.recipe.Recipe;
import org.recipefinder.recipefinder.recipe.dto.RecipeDTO;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface RecipeMapper {

    RecipeMapper INSTANCE = Mappers.getMapper(RecipeMapper.class);

    RecipeDTO mapToRecipeDTO(Recipe recipe);
}
