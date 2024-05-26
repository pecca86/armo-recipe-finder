package org.recipefinder.recipefinder.auth;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.recipefinder.recipefinder.auth.dto.AuthenticationRequest;
import org.recipefinder.recipefinder.auth.dto.RegisterRequest;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;
import org.recipefinder.recipefinder.customer.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.mockito.BDDMockito.given;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationTest {

    private static final String EMAIL = "jd@jd.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PASSWORD = "password";

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

    @Autowired
    CustomerRepository customerRepository;
    @MockBean
    AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1/auth";
        customerRepository.deleteAll();
        customerRepository.save(new Customer(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, Role.ROLE_USER));
        customerRepository.flush();
    }

    @Test
    void should_return_http_200_when_valid_registration_data() {
        RegisterRequest registerRequest = new RegisterRequest(FIRST_NAME, LAST_NAME, "new@mail.com", PASSWORD);
        RestAssured.given()
                   .contentType("application/json")
                   .body(registerRequest)
                   .when()
                   .post("/register")
                   .then()
                   .statusCode(200);
    }

    @Test
    void should_return_http_400_when_registering_using_existing_email() {
        RegisterRequest registerRequest = new RegisterRequest(FIRST_NAME, LAST_NAME, EMAIL, PASSWORD);
        RestAssured.given()
                   .contentType("application/json")
                   .body(registerRequest)
                   .when()
                   .post("/register")
                   .then()
                   .statusCode(400);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "John, Doe, jd@jd.com,",
            "John, Doe, , password",
            "John, , jd@jd.com, password",
            ", Doe, jd@jd.com, password"
    })
    void should_return_http_401_when_invalid_registration_data(String firstName, String lastName, String email, String password) {
        RegisterRequest registerRequest = new RegisterRequest(firstName, lastName, email, password);
        RestAssured.given()
                   .contentType("application/json")
                   .body(registerRequest)
                   .when()
                   .post("/register")
                   .then()
                   .statusCode(401)
                   .and()
                   .body(".", hasKey("message"))
                   .and()
                   .body("message", equalTo("Please provide all required fields (email, firstName, lastName, password)"));
    }

    @Test
    void should_return_http_401_when_login_with_wrong_password() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(EMAIL, "wrongpassword");
        given(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(EMAIL, "wrongpassword")))
                .willThrow(new BadCredentialsException("Bad credentials"));
        RestAssured.given()
                   .contentType("application/json")
                   .body(authenticationRequest)
                   .when()
                   .post("/login")
                   .then()
                   .statusCode(401)
                   .and()
                   .body(".", hasKey("message"))
                   .and()
                   .body("message", equalTo("Bad credentials"));
    }

    @Test
    void should_return_http_401_when_login_with_wrong_email() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("wrongemail@jd.com", PASSWORD);
        given(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("wrong@email.com", PASSWORD)))
                .willThrow(new BadCredentialsException("Bad credentials"));
        RestAssured.given()
                   .contentType("application/json")
                   .body(authenticationRequest)
                   .when()
                   .post("/login")
                   .then()
                   .statusCode(404)
                   .and()
                   .body(".", hasKey("message"))
                   .and()
                   .body("message", equalTo("Invalid email or password"));
    }

    @Test
    void should_return_http_200_when_login_with_correct_credentials() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(EMAIL, PASSWORD);
        RestAssured.given()
                   .contentType("application/json")
                   .body(authenticationRequest)
                   .when()
                   .post("/login")
                   .then()
                   .statusCode(200)
                   .and()
                   .body(".", hasKey("message"))
                   .and()
                   .body("message", equalTo("200 OK"));
    }
}
