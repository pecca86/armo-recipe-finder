package org.recipefinder.recipefinder.auth;

import org.recipefinder.recipefinder.auth.dto.AuthenticationRequest;
import org.recipefinder.recipefinder.auth.dto.AuthenticationResponse;
import org.recipefinder.recipefinder.auth.dto.NewPasswordRequest;
import org.recipefinder.recipefinder.auth.dto.RegisterRequest;
import org.recipefinder.recipefinder.config.JwtService;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;
import org.recipefinder.recipefinder.customer.Role;
import org.recipefinder.recipefinder.exceptions.customer.CustomerAlreadyExistsException;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoggedInCustomerService loggedInCustomerService;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager, CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService, LoggedInCustomerService loggedInCustomerService) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loggedInCustomerService = loggedInCustomerService;
    }

    public AuthenticationResponse register(RegisterRequest registerRequest) {
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

    public Customer getLoggedInCustomer(Authentication authentication) throws CustomerNotFoundException {
        String email = authentication.getName();
        return customerRepository.findCustomerByEmail(email)
                                 .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

    }

    public void updatePassword(NewPasswordRequest passwordRequest, Authentication authentication) {
        Customer customerToUpdate = loggedInCustomerService.getLoggedInCustomer(authentication);
        customerToUpdate.setPassword(passwordEncoder.encode(passwordRequest.password()));
        customerRepository.save(customerToUpdate);
    }
}
