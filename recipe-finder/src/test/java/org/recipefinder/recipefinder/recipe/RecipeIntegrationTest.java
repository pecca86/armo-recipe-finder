package org.recipefinder.recipefinder.recipe;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.recipefinder.recipefinder.auth.LoggedInCustomerService;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;
import org.recipefinder.recipefinder.customer.Role;
import org.recipefinder.recipefinder.recipe.dto.RecipeDTO;
import org.recipefinder.recipefinder.recipe.dto.RecipeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cache.type=none"
)
@ActiveProfiles("test")
class RecipeIntegrationTest {

    private final String CUSTOMER1_EMAIL = "jd@jd.com";
    private final String CUSTOMER1_FIRST_NAME = "John";
    private final String CUSTOMER1_LAST_NAME = "Doe";
    private final String CUSTOMER1_PASSWORD = "password";
    private final String CUSTOMER1_DESCRIPTION = "Recipe 1";
    private final int CUSTOMER1_NUM_SERVINGS = 4;
    private final boolean CUSTOMER1_IS_VEGAN = true;
    private final String CUSTOMER1_INGREDIENT_1 = "ingredient1";
    private final String CUSTOMER1_INGREDIENT_2 = "ingredient2";

    private final String CUSTOMER2_EMAIL = "second@jd.com";
    private final String CUSTOMER2_FIRST_NAME = "Joe";
    private final String CUSTOMER2_LAST_NAME = "Strummer";
    private final String CUSTOMER2_PASSWORD = "secret";
    private final String CUSTOMER_2_DESCRIPTION = "Customer 2 Description";
    private final boolean CUSTOMER2_IS_VEGAN = false;
    private final int CUSTOMER2_NUM_SERVINGS = 10;
    private final String CUSTOMER_2_INGREDIENT_1 = "customer 2 ingredient 1";
    private final String CUSTOMER_2_INGREDIENT_2 = "customer 2 ingredient 2";

    private Long currentRecipeId;

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    LoggedInCustomerService loggedInCustomerService;
    @Autowired
    RecipePagingRepository recipePagingRepository;
    @MockBean
    AuthenticationManager authenticationManager;

    Customer customer1;
    Customer customer2;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        customerRepository.deleteAll();
        setUpCustomersAndPersist();
    }

    /*
     * GET RECIPE TESTS
     */
    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_200_when_getting_a_recipe_that_exists_for_logged_in_user() throws Exception {
        RecipeDTO recipeDTO = new RecipeDTO(currentRecipeId, CUSTOMER1_DESCRIPTION, CUSTOMER1_IS_VEGAN, CUSTOMER1_NUM_SERVINGS, Arrays.asList(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2));
        RecipeResponse expected = new RecipeResponse(HttpStatus.OK.value(), "Recipe retrieved successfully", recipeDTO);

        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes/" + currentRecipeId)
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_404_when_getting_a_recipe_that_does_not_exists_for_logged_in_user() throws Exception {
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes/2")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().is(404));
    }

    @Test
    void should_return_403_when_trying_to_access_without_credentials() throws Exception {
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes/1")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().is(403));
    }

    /*
     * GET RECIPES TESTS
     */
    @Test
    void should_return_403_when_getting_recipes_for_unauthenticated_user() throws Exception {
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().is(403));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_200_when_getting_recipes_for_logged_in_user() throws Exception {
        // 10 recipes less due to caching
        String expectedJson = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"size\":100,\"content\":[{\"id\":27,\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"unsorted\":true,\"sorted\":false},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"unsorted\":true,\"sorted\":false},\"offset\":0,\"unpaged\":false,\"paged\":true},\"first\":true,\"last\":true,\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedJson));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_one_result_when_is_vegan_is_true() throws Exception {
        String expectedJsonIsVeganTrue = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"unsorted\":true,\"sorted\":false},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"unsorted\":true,\"sorted\":false},\"offset\":0,\"unpaged\":false,\"paged\":true},\"first\":true,\"last\":true,\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?isVegan=true")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedJsonIsVeganTrue));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_zero_results_when_is_vegan_is_false() throws Exception {
        String expectedJsonIsVeganFalse = "{\"recipes\":{\"totalElements\":0,\"totalPages\":0,\"first\":true,\"last\":true,\"size\":100,\"content\":[],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":0,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":true}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?isVegan=false")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedJsonIsVeganFalse));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "recipe",
            "Recipe",
            "ReCiPE",
            "1"
    })
    void should_return_one_result_when_description_contains_input_text_after_applied_string_normalization(String searchText) throws Exception {
        String expectedJsonDescriptionTextMatch = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?description=" + searchText)
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedJsonDescriptionTextMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_zero_results_when_no_text_match_for_description() throws Exception {
        String expectedJsonDescriptionTextNoMatch = "{\"recipes\":{\"totalElements\":0,\"totalPages\":0,\"first\":true,\"last\":true,\"size\":100,\"content\":[],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":0,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":true}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?description=notfound")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedJsonDescriptionTextNoMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "Ingredient1,Ingredient2",
            "Ingredient1,",
            "Ingredient2,",
    })
    void should_return_one_result_when_matching_ingredients(String ingredient1, String ingredient2) throws Exception {
        String expectedIngredientsMatch = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?ingredients=" + ingredient1 + (ingredient2 != null ? "," + ingredient2 : ""))
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedIngredientsMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "nonsense1,nonsense2",
            "Ingredient10,",
            "Ingredient20,",
    })
    void should_return_zero_results_when_no_matching_ingredients(String ingredient1, String ingredient2) throws Exception {
        String expectedIngredientsMatch = "{\"recipes\":{\"totalElements\":0,\"totalPages\":0,\"first\":true,\"last\":true,\"size\":100,\"content\":[],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":0,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":true}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?ingredients=" + (ingredient1 != null ? ingredient1 : "") + (ingredient2 != null ? "," + ingredient2 : ""))
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedIngredientsMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_one_result_when_filtering_on_ingredients_without_values() throws Exception {
        String expectedIngredientsMatch = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?ingredients=")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedIngredientsMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "Ingredient1,Ingredient2",
            "Ingredient1,",
            "Ingredient2,",
    })
    void should_return_zero_results_excluding_ingredients_filter_matches(String ingredient1, String ingredient2) throws Exception {
        String expectedExcludeIngredientsMatch = "{\"recipes\":{\"totalElements\":0,\"totalPages\":0,\"first\":true,\"last\":true,\"size\":100,\"content\":[],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":0,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":true}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?excludeIngredients=" + (ingredient1 != null ? ingredient1 : "") + (ingredient2 != null ? "," + ingredient2 : ""))
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedExcludeIngredientsMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "nonsense1,nonsense2",
            "Ingredient10,",
            "Ingredient20,",
    })
    void should_return_one_result_excluding_ingredients_filter_does_not_match(String ingredient1, String ingredient2) throws Exception {
        String expectedExcludedIngredientsNoMatch = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?excludeIngredients=" + (ingredient1 != null ? ingredient1 : "") + (ingredient2 != null ? "," + ingredient2 : ""))
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedExcludedIngredientsNoMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_correct_pageNumber_and_pageSize() throws Exception {
        String expectedPageNumberAndPageSizeResponse = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?page=0&pageSize=100")
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedPageNumberAndPageSizeResponse));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "numServings,4,ingredients,Ingredient1",
            "isVegan,true,excludeIngredients,flour",
            "description,recipe,ingredients,Ingredient2",
    })
    void should_return_one_result_when_combined_filters_match(String filter1, String filterValue1, String filter2, String filterValue2) throws Exception {
        String expectedCombinedFiltersMatch = "{\"recipes\":{\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"size\":100,\"content\":[{\"id\":" + currentRecipeId + ",\"description\":\"Recipe 1\",\"ingredients\":[\"ingredient1\",\"ingredient2\"],\"is_vegan\":true,\"num_servings\":4}],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":1,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":false}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?" + filter1 + "=" + filterValue1 + "&" + filter2 + "=" + filterValue2)
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedCombinedFiltersMatch));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "numServings,40,ingredients,Ingredient1",
            "isVegan,false,excludeIngredients,flour",
            "description,recipe,ingredients,Ingredient20",
    })
    void should_return_no_results_when_combined_filters_do_not_match(String filter1, String filterValue1, String filter2, String filterValue2) throws Exception {
        String expectedCombinedFiltersNoMatch = "{\"recipes\":{\"totalElements\":0,\"totalPages\":0,\"first\":true,\"last\":true,\"size\":100,\"content\":[],\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":0,\"pageable\":{\"pageNumber\":0,\"pageSize\":100,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"paged\":true,\"unpaged\":false},\"empty\":true}}";
        mvc.perform(
                   get("http://localhost:" + port + "/api/v1/recipes?" + filter1 + "=" + filterValue1 + "&" + filter2 + "=" + filterValue2)
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().isOk())
           .andExpect(content().json(expectedCombinedFiltersNoMatch));
    }

    /*
     * CREATE RECIPE TESTS
     */
    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_201_and_recipe_data_when_creating_new_recipe_with_valid_data() throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(
                Map.of(
                        "description", CUSTOMER1_DESCRIPTION,
                        "is_vegan", CUSTOMER1_IS_VEGAN,
                        "num_servings", CUSTOMER1_NUM_SERVINGS,
                        "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
                )
        );

        // Plus two since we manually create two recipes in the setUp method
        RecipeDTO recipeDTO = new RecipeDTO(currentRecipeId + 2, CUSTOMER1_DESCRIPTION, CUSTOMER1_IS_VEGAN, CUSTOMER1_NUM_SERVINGS, Arrays.asList(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2));
        RecipeResponse expected = new RecipeResponse(HttpStatus.CREATED.value(), "Recipe created successfully", recipeDTO);

        mvc.perform(
                   post("http://localhost:" + port + "/api/v1/recipes")
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(201))
           .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    void should_return_403_when_trying_to_create_a_new_recipe_without_logging_in() throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(
                Map.of(
                        "description", CUSTOMER1_DESCRIPTION,
                        "is_vegan", CUSTOMER1_IS_VEGAN,
                        "num_servings", CUSTOMER1_NUM_SERVINGS,
                        "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
                )
        );

        mvc.perform(
                   post("http://localhost:" + port + "/api/v1/recipes")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(403));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "no_description;{\"statusCode\":400,\"message\":\"Description is required\"}",
            "no_is_vegan;{\"statusCode\":400,\"message\":\"Is vegan is required\"}",
            "no_num_servings;{\"statusCode\":400,\"message\":\"Number of servings is required\"}",
            "no_ingredients;{\"statusCode\":400,\"message\":\"Ingredients are required\"}"
    }, delimiter = ';')
    void should_return_400_when_trying_to_create_a_new_recipe_with_invalid_data(String missingValue, String expectedResponse) throws Exception {

        Map<String, Object> payload = buildRecipePayload(missingValue);
        String jsonPayload = objectMapper.writeValueAsString(payload);

        mvc.perform(
                   post("http://localhost:" + port + "/api/v1/recipes")
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(400))
           .andExpect(content().json(expectedResponse));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_400_when_trying_to_create_a_new_recipe_num_serving_lt_1() throws Exception {
        String expectedResponse = "{\"statusCode\":400,\"message\":\"Number of servings must be 1 or greater\"}";
        Map<String, Object> payload = Map.of(
                "description", CUSTOMER1_DESCRIPTION,
                "num_servings", 0,
                "is_vegan", CUSTOMER1_IS_VEGAN,
                "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
        );
        String jsonPayload = objectMapper.writeValueAsString(payload);

        mvc.perform(
                   post("http://localhost:" + port + "/api/v1/recipes")
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(400))
           .andExpect(content().json(expectedResponse));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_400_when_trying_to_create_a_new_recipe_with_invalid_data_type() throws Exception {
        Map<String, Object> payload = Map.of(
                "description", CUSTOMER1_DESCRIPTION,
                "num_servings", CUSTOMER1_NUM_SERVINGS,
                "is_vegan", "yes",
                "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
        );
        String jsonPayload = objectMapper.writeValueAsString(payload);

        mvc.perform(
                   post("http://localhost:" + port + "/api/v1/recipes")
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(400));
    }

    /*
     * UPDATE RECIPE TESTS
     */
    @Test
    void should_return_403_when_updating_recipe_without_logging_in() throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(
                Map.of(
                        "description", "new description",
                        "is_vegan", "new is_vegan",
                        "num_servings", "new num_servings",
                        "ingredients", List.of("new_ingredient_1")
                )
        );

        mvc.perform(
                   put("http://localhost:" + port + "/api/v1/recipes/1" + currentRecipeId)
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(403));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_201_and_recipe_data_when_updating_a_recipe_with_valid_data() throws Exception {
        Long originalRecipeId = customer1.getRecipes().get(0).getId();

        String jsonPayload = objectMapper.writeValueAsString(
                Map.of(
                        "description", "new description",
                        "is_vegan", false,
                        "num_servings", 99,
                        "ingredients", List.of("new_ingredient_1")
                )
        );

        RecipeDTO recipeDTO = new RecipeDTO(null, "new description", false, 99, List.of("new_ingredient_1"));
        RecipeResponse expected = new RecipeResponse(HttpStatus.CREATED.value(), "Recipe updated successfully", recipeDTO);

        mvc.perform(
                   put("http://localhost:" + port + "/api/v1/recipes/" + originalRecipeId)
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(201))
           .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @ParameterizedTest
    @CsvSource(value = {
            "no_description;{\"statusCode\":400,\"message\":\"Description is required\"}",
            "no_is_vegan;{\"statusCode\":400,\"message\":\"Is vegan is required\"}",
            "no_num_servings;{\"statusCode\":400,\"message\":\"Number of servings is required\"}",
            "no_ingredients;{\"statusCode\":400,\"message\":\"Ingredients are required\"}"
    }, delimiter = ';')
    void should_return_400_when_trying_to_update_a_recipe_with_invalid_data(String missingValue, String expectedResponse) throws Exception {
        Long originalRecipeId = customer1.getRecipes().get(0).getId();
        Map<String, Object> payload = buildRecipePayload(missingValue);
        String jsonPayload = objectMapper.writeValueAsString(payload);

        mvc.perform(
                   put("http://localhost:" + port + "/api/v1/recipes/" + originalRecipeId)
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(400))
           .andExpect(content().json(expectedResponse));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_404_when_updating_a_recipe_that_does_not_exists_for_logged_in_user() throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(
                Map.of(
                        "description", "new description",
                        "is_vegan", false,
                        "num_servings", 99,
                        "ingredients", List.of("new_ingredient_1")
                )
        );

        mvc.perform(
                   put("http://localhost:" + port + "/api/v1/recipes/2")
                           .header("Authorization", "Bearer")
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(jsonPayload)
           )
           .andExpect(status().is(404));
    }

    /*
     * DELETE RECIPE TESTS
     */

    @Test
    void should_return_403_when_trying_to_delete_without_logging_in() throws Exception {
        mvc.perform(
                   delete("http://localhost:" + port + "/api/v1/recipes/2")
           )
           .andExpect(status().is(403));
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, password = CUSTOMER1_PASSWORD, roles = "USER")
    @Test
    void should_return_200_when_successfully_deleting_recipe() throws Exception {

        String expectedResponse = new ObjectMapper()
                .writeValueAsString(new RecipeResponse(HttpStatus.OK.value(), "Recipe with id " + currentRecipeId + " deleted successfully", null));

        mvc.perform(
                   delete("http://localhost:" + port + "/api/v1/recipes/" + currentRecipeId)
                           .header("Authorization", "Bearer")
           )
           .andExpect(status().is(200))
           .andExpect(content().json(expectedResponse));
    }

    private void setUpCustomersAndPersist() {
        customer1 = new Customer(CUSTOMER1_EMAIL, CUSTOMER1_FIRST_NAME, CUSTOMER1_LAST_NAME, CUSTOMER1_PASSWORD, Role.ROLE_USER);
        Recipe customer1Recipe = new Recipe();
        customer1Recipe.setDescription(CUSTOMER1_DESCRIPTION);
        customer1Recipe.setIsVegan(CUSTOMER1_IS_VEGAN);
        customer1Recipe.setNumServings(CUSTOMER1_NUM_SERVINGS);
        customer1Recipe.setIngredients(Arrays.asList(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2));
        customer1Recipe.setCustomer(customer1);
        customer1.setRecipes(List.of(customer1Recipe));

        customer2 = new Customer(CUSTOMER2_EMAIL, CUSTOMER2_FIRST_NAME, CUSTOMER2_LAST_NAME, CUSTOMER2_PASSWORD, Role.ROLE_USER);
        Recipe customer2Recipe = new Recipe();
        customer2Recipe.setDescription(CUSTOMER_2_DESCRIPTION);
        customer2Recipe.setIsVegan(CUSTOMER2_IS_VEGAN);
        customer2Recipe.setNumServings(CUSTOMER2_NUM_SERVINGS);
        customer2Recipe.setIngredients(Arrays.asList(CUSTOMER_2_INGREDIENT_1, CUSTOMER_2_INGREDIENT_2));
        customer2Recipe.setCustomer(customer2);
        customer2.setRecipes(List.of(customer2Recipe));

        customerRepository.saveAllAndFlush(List.of(customer1, customer2));
        this.currentRecipeId = customer1.getRecipes().get(0).getId();
    }

    private Map<String, Object> buildRecipePayload(String missingValue) {
        final String missing = missingValue;
        return switch (missing) {
            case "no_description" -> Map.of(
                    "is_vegan", CUSTOMER1_IS_VEGAN,
                    "num_servings", CUSTOMER1_NUM_SERVINGS,
                    "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
            );
            case "no_is_vegan" -> Map.of(
                    "description", CUSTOMER1_DESCRIPTION,
                    "num_servings", CUSTOMER1_NUM_SERVINGS,
                    "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
            );
            case "no_num_servings" -> Map.of(
                    "description", CUSTOMER1_DESCRIPTION,
                    "is_vegan", CUSTOMER1_IS_VEGAN,
                    "ingredients", List.of(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2)
            );
            case "no_ingredients" -> Map.of(
                    "description", CUSTOMER1_DESCRIPTION,
                    "is_vegan", CUSTOMER1_IS_VEGAN,
                    "num_servings", CUSTOMER1_NUM_SERVINGS
            );
            default -> throw new IllegalStateException("Unexpected value: " + missing);
        };
    }
}
