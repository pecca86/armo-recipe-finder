package org.recipefinder.recipefinder.recipe;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.recipefinder.recipefinder.auth.LoggedInCustomerService;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;

import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.recipefinder.recipefinder.exceptions.recipe.RecipeAccessException;
import org.recipefinder.recipefinder.exceptions.recipe.RecipeNotFoundException;
import org.recipefinder.recipefinder.recipe.dto.RecipeResponse;
import org.recipefinder.recipefinder.recipe.dto.RecipesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CustomerRepository customerRepository;
    private final LoggedInCustomerService loggedInCustomerService;
    private final EntityManager entityManager;

    @Autowired
    public RecipeService(CustomerRepository customerRepository, RecipeRepository recipeRepository, LoggedInCustomerService loggedInCustomerService, EntityManager entityManager) {
        this.customerRepository = customerRepository;
        this.recipeRepository = recipeRepository;
        this.loggedInCustomerService = loggedInCustomerService;
        this.entityManager = entityManager;
    }

    public RecipeResponse getRecipe(Authentication authentication, Long recipeId) {
        Recipe recipe = loggedInCustomerService.getLoggedInCustomer(authentication).getRecipes().stream()
                                               .filter(r -> r.getId().equals(recipeId))
                                               .findFirst()
                                               .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
        return new RecipeResponse(HttpStatus.OK.value(), "Recipe retrieved successfully", recipe);
    }

    public RecipesResponse getRecipes(Authentication authentication) throws CustomerNotFoundException {
        return new RecipesResponse(
                HttpStatus.OK.value(),
                "Recipes retrieved successfully",
                loggedInCustomerService.getLoggedInCustomer(authentication).getRecipes()
        );
    }

    @Transactional
    public RecipeResponse createRecipe(Authentication authentication, Recipe recipe) {
        if (recipe == null) {
            throw new RecipeAccessException("Recipe cannot be null");
        }

        Customer loggedInCustomer = loggedInCustomerService.getLoggedInCustomer(authentication);
        loggedInCustomer.addRecipe(recipe);
        entityManager.persist(recipe); // so we can get the ID of the recipe
        customerRepository.save(loggedInCustomer);
        return new RecipeResponse(HttpStatus.CREATED.value(), "Recipe created successfully", recipe);
    }

    public RecipeResponse updateRecipe(Authentication authentication, Recipe recipe, Long recipeId) {
        if (recipe == null) {
            throw new RecipeAccessException("Missing recipe data");
        }
        loggedInCustomerService.getLoggedInCustomer(authentication).getRecipes().stream()
                               .filter(r -> r.getId().equals(recipeId))
                               .findFirst()
                               .ifPresentOrElse(r -> {
                                   r.setDescription(recipe.getDescription());
                                   r.setIsVegan(recipe.getIsVegan());
                                   r.setNumServings(recipe.getNumServings());
                                   r.setIngredients(recipe.getIngredients());
                                   customerRepository.save(loggedInCustomerService.getLoggedInCustomer(authentication));
                               }, () -> {
                                   throw new RecipeNotFoundException("Recipe with id " + recipeId + " not found");
                               });
        return new RecipeResponse(HttpStatus.OK.value(), "Recipe updated successfully", recipe);
    }

    public RecipeResponse deleteRecipe(Authentication authentication, Long recipeId) {
        Customer customer = loggedInCustomerService.getLoggedInCustomer(authentication);
        boolean wasDeleted = customer.getRecipes()
                                     .removeIf(recipe -> recipe.getId().equals(recipeId));

        if (!wasDeleted) {
            throw new RecipeNotFoundException("Recipe with id " + recipeId + " not found");
        }
        customerRepository.save(customer);
        return new RecipeResponse(HttpStatus.OK.value(), "Recipe with id " + recipeId + " deleted successfully", null);
    }
}
