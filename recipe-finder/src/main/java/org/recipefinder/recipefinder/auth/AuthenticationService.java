package org.recipefinder.recipefinder.auth;

import org.recipefinder.recipefinder.auth.dto.AuthenticationRequest;
import org.recipefinder.recipefinder.auth.dto.AuthenticationResponse;
import org.recipefinder.recipefinder.auth.dto.RegisterRequest;
import org.recipefinder.recipefinder.config.JwtService;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;
import org.recipefinder.recipefinder.customer.Role;
import org.recipefinder.recipefinder.exceptions.AuthenticationException;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager, CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        if (isValidRegistrationData(registerRequest)) {
            throw new AuthenticationException("Please provide all required fields (email, firstName, lastName, password)");
        }

        var customer = new Customer(
                registerRequest.email(),
                registerRequest.firstName(),
                registerRequest.lastName(),
                passwordEncoder.encode(registerRequest.password()),
                Role.ROLE_USER
        );

        customerRepository.save(customer);

        var jwtToken = jwtService.generateJwtToken(customer);
        return new AuthenticationResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(), jwtToken);
    }

    private boolean isValidRegistrationData(RegisterRequest registerRequest) {
        return registerRequest.password() == null
                || registerRequest.password().isBlank()
                || registerRequest.email() == null
                || registerRequest.email().isBlank()
                || registerRequest.firstName() == null
                || registerRequest.firstName().isBlank()
                || registerRequest.lastName() == null
                || registerRequest.lastName().isBlank();
    }

    public AuthenticationResponse login(AuthenticationRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        var customer = customerRepository.findCustomerByEmail(loginRequest.email())
                                         .orElseThrow(() -> new CustomerNotFoundException("Invalid email or password"));

        var jwtToken = jwtService.generateJwtToken(customer);
        return new AuthenticationResponse(HttpStatus.OK.value(), HttpStatus.OK.toString(), jwtToken);
    }
}
