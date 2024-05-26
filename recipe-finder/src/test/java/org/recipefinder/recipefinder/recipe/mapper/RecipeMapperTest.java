package org.recipefinder.recipefinder.recipe.mapper;

import org.junit.jupiter.api.Test;
import org.recipefinder.recipefinder.recipe.Recipe;
import org.recipefinder.recipefinder.recipe.dto.RecipeDTO;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeMapperTest {

    @Test
    void shouldMapRecipeToRecipeDto() {
        //given
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setDescription("Test description");
        recipe.setIsVegan(true);
        recipe.setNumServings(4);
        recipe.setIngredients(List.of("ingredient1", "ingredient2", "ingredient3"));
        //when
        RecipeDTO recipeDTO = RecipeMapper.INSTANCE.mapToRecipeDTO(recipe);
        //then
        assertThat(recipeDTO).isNotNull();
        assertThat(recipeDTO.id()).isEqualTo(1L);
        assertThat(recipeDTO.description()).isEqualTo("Test description");
        assertThat(recipeDTO.isVegan()).isTrue();
        assertThat(recipeDTO.numServings()).isEqualTo(4);
        assertThat(recipeDTO.ingredients()).containsExactly("ingredient1", "ingredient2", "ingredient3");
    }

}
