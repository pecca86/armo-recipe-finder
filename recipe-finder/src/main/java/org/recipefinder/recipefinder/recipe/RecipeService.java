package org.recipefinder.recipefinder.recipe;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.recipefinder.recipefinder.auth.LoggedInCustomerService;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;

import org.recipefinder.recipefinder.exceptions.recipe.RecipeAccessException;
import org.recipefinder.recipefinder.exceptions.recipe.RecipeNotFoundException;
import org.recipefinder.recipefinder.recipe.dto.PaginatedRecipeResponse;
import org.recipefinder.recipefinder.recipe.dto.RecipeDTO;
import org.recipefinder.recipefinder.recipe.dto.RecipeResponse;
import org.recipefinder.recipefinder.recipe.mapper.RecipeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private final CustomerRepository customerRepository;
    private final LoggedInCustomerService loggedInCustomerService;
    private final EntityManager entityManager;
    private final RecipePagingRepository recipePagingRepository;

    @Autowired
    public RecipeService(CustomerRepository customerRepository, LoggedInCustomerService loggedInCustomerService, EntityManager entityManager, RecipePagingRepository recipePagingRepository) {
        this.customerRepository = customerRepository;
        this.loggedInCustomerService = loggedInCustomerService;
        this.entityManager = entityManager;
        this.recipePagingRepository = recipePagingRepository;
    }

    public RecipeResponse getRecipe(Authentication authentication, Long recipeId) {
        Recipe recipe = loggedInCustomerService.getLoggedInCustomer(authentication).getRecipes().stream()
                                               .filter(r -> r.getId().equals(recipeId))
                                               .findFirst()
                                               .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
        return new RecipeResponse(HttpStatus.OK.value(), "Recipe retrieved successfully", recipe);
    }

    public PaginatedRecipeResponse getRecipes(Authentication authentication,
                                              String description,
                                              Boolean isVegan,
                                              Integer numServings,
                                              String ingredients,
                                              String excludeIngredients,
                                              int page,
                                              int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);
        Customer loggedInCustomer = loggedInCustomerService.getLoggedInCustomer(authentication);

        Specification<Recipe> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("customer"), loggedInCustomer));
            if (description != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }
            if (isVegan != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVegan"), isVegan));
            }
            if (numServings != null) {
                predicates.add(criteriaBuilder.equal(root.get("numServings"), numServings));
            }
            if (ingredients != null) {
                List<String> ingredientsList = List.of(ingredients.split(","));
                ingredientsList.forEach(ingredient -> predicates.add(criteriaBuilder.like(root.get("ingredients"), "%" + ingredient.toLowerCase() + "%")));
            }
            if (excludeIngredients != null) {
                List<String> excludeIngredientsList = List.of(excludeIngredients.split(","));
                excludeIngredientsList.forEach(ingredient -> predicates.add(criteriaBuilder.notLike(root.get("ingredients"), "%" + ingredient.toLowerCase() + "%")));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Recipe> recipes = recipePagingRepository.findAll(spec, pageable);
        return new PaginatedRecipeResponse(recipes);
    }

    @Transactional
    public RecipeResponse createRecipe(Authentication authentication, RecipeDTO recipeDTO) {
        if (recipeDTO == null) {
            throw new RecipeAccessException("Recipe cannot be null");
        }

        Recipe recipe = RecipeMapper.INSTANCE.mapToRecipe(recipeDTO);

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
