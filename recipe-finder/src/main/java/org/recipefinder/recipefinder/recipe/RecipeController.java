package org.recipefinder.recipefinder.recipe;

import jakarta.validation.Valid;
import org.recipefinder.recipefinder.recipe.dto.PaginatedRecipeResponse;
import org.recipefinder.recipefinder.recipe.dto.RecipeDTO;
import org.recipefinder.recipefinder.recipe.dto.RecipeResponse;
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
    public ResponseEntity<PaginatedRecipeResponse> getRecipes(Authentication authentication,
                                                               @RequestParam(required = false) String description,
                                                               @RequestParam(required = false) Boolean isVegan,
                                                               @RequestParam(required = false) Integer numServings,
                                                               @RequestParam(required = false) String ingredients,
                                                               @RequestParam(required = false) String excludeIngredients,
                                                               @RequestParam(required = false, defaultValue = "0") int page,
                                                               @RequestParam(required = false, defaultValue = "100") int pageSize) {
        PaginatedRecipeResponse response = recipeService.getRecipes(authentication, description, isVegan, numServings, ingredients, excludeIngredients, page, pageSize);
        return ResponseEntity.ok(response);
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
