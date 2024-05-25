package org.recipefinder.recipefinder.recipe;

import jakarta.validation.Valid;
import org.recipefinder.recipefinder.recipe.dto.RecipeResponse;
import org.recipefinder.recipefinder.recipe.dto.RecipesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("{recipeId}")
    public ResponseEntity<RecipeResponse> getRecipe(@CurrentSecurityContext(expression = "authentication") Authentication authentication,
                                                    @PathVariable Long recipeId) {
        return ResponseEntity.status(200).body(recipeService.getRecipe(authentication, recipeId));
    }

    @GetMapping
    public ResponseEntity<RecipesResponse> getRecipes(@CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return ResponseEntity.status(200).body(recipeService.getRecipes(authentication));
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@CurrentSecurityContext(expression = "authentication") Authentication authentication,
                                                       @RequestBody @Valid Recipe recipe) {
        return ResponseEntity.status(201).body(recipeService.createRecipe(authentication, recipe));
    }

    @PutMapping("{recipeId}")
    public ResponseEntity<RecipeResponse> updateRecipe(@CurrentSecurityContext(expression = "authentication") Authentication authentication,
                                                       @RequestBody @Valid Recipe recipe,
                                                       @PathVariable Long recipeId) {
        return ResponseEntity.status(201).body(recipeService.updateRecipe(authentication, recipe, recipeId));
    }

    @DeleteMapping("{recipeId}")
    public ResponseEntity<RecipeResponse> deleteRecipe(@CurrentSecurityContext(expression = "authentication") Authentication authentication,
                                                       @PathVariable Long recipeId) {
        return ResponseEntity.status(200).body(recipeService.deleteRecipe(authentication, recipeId));
    }

}
