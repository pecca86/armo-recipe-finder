package org.recipefinder.recipefinder.auth;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.recipefinder.recipefinder.auth.dto.*;
import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final Bucket bucket;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;

        Bandwidth bandwidth = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                            .addLimit(bandwidth)
                            .build();
    }

    @PostMapping(path = "/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(authenticationService.register(registerRequest));
        }
        return ResponseEntity.status(429).body(new AuthenticationResponse(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.toString(), ""));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest loginRequest) throws CustomerNotFoundException {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(authenticationService.login(loginRequest));
        }
        return ResponseEntity.status(429).body(new AuthenticationResponse(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.toString(), ""));
    }

    @GetMapping("/me")
    public ResponseEntity<Customer> getCustomer(@CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return ResponseEntity.ok(authenticationService.getLoggedInCustomer(authentication));

    }

    @PostMapping("/password")
    public ResponseEntity<String> changePassword(@CurrentSecurityContext(expression = "authentication") Authentication authentication,
                                                 @RequestBody NewPasswordRequest password) {
        if (bucket.tryConsume(1)) {
            authenticationService.updatePassword(password, authentication);
            return ResponseEntity.ok("Password updated");
        }
        return ResponseEntity.status(429).body("Too many requests, please try again later");
    }
}