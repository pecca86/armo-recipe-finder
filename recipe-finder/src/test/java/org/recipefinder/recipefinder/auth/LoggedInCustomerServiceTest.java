package org.recipefinder.recipefinder.auth;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;
import org.recipefinder.recipefinder.customer.Role;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.recipefinder.recipefinder.recipe.Recipe;
import org.recipefinder.recipefinder.recipe.RecipePagingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoggedInCustomerServiceTest {

    private final String CUSTOMER1_EMAIL = "jd@jd.com";
    private final String CUSTOMER1_FIRST_NAME = "John";
    private final String CUSTOMER1_LAST_NAME = "Doe";
    private final String CUSTOMER1_PASSWORD = "password";
    private final String CUSTOMER1_DESCRIPTION = "Recipe 1";
    private final int CUSTOMER1_NUM_SERVINGS = 4;
    private final boolean CUSTOMER1_IS_VEGAN = true;
    private final String CUSTOMER1_INGREDIENT_1 = "ingredient1";
    private final String CUSTOMER1_INGREDIENT_2 = "ingredient2";

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
    @Mock
    Authentication authentication;
    Customer customer;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        customerRepository.deleteAll();

        setUpCustomer();
    }

    @Transactional
    @WithMockUser(username = CUSTOMER1_EMAIL, roles = "USER")
    @Test
    void should_find_logged_in_customer() {
        given(authentication.getName()).willReturn(CUSTOMER1_EMAIL);
        Customer loggedInCustomer = loggedInCustomerService.getLoggedInCustomer(authentication);
        assertThat(loggedInCustomer).isEqualTo(customer);
    }

    @WithMockUser(username = CUSTOMER1_EMAIL, roles = "USER")
    @Test
    void should_throw_error_when_customer_not_found() {
        given(authentication.getName()).willReturn(null);
        assertThrows(CustomerNotFoundException.class, () -> loggedInCustomerService.getLoggedInCustomer(authentication));
    }

    private void setUpCustomer() {
        customer = new Customer(CUSTOMER1_EMAIL, CUSTOMER1_FIRST_NAME, CUSTOMER1_LAST_NAME, CUSTOMER1_PASSWORD, Role.ROLE_USER);
        Recipe customer1Recipe = new Recipe();
        customer1Recipe.setDescription(CUSTOMER1_DESCRIPTION);
        customer1Recipe.setIsVegan(CUSTOMER1_IS_VEGAN);
        customer1Recipe.setNumServings(CUSTOMER1_NUM_SERVINGS);
        customer1Recipe.setIngredients(Arrays.asList(CUSTOMER1_INGREDIENT_1, CUSTOMER1_INGREDIENT_2));
        customer1Recipe.setCustomer(customer);
        customer.setRecipes(List.of(customer1Recipe));

        customerRepository.saveAndFlush(customer);
    }
}
