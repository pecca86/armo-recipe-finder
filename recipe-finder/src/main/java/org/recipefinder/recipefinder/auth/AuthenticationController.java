package org.recipefinder.recipefinder.auth;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.recipefinder.recipefinder.auth.dto.*;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final Bucket bucket;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    @Value("${rate.limit.capacity}") int rateLimitCapacity,
                                    @Value("${rate.limit.refill.time}") int rateLimitRefillTime,
                                    @Value("${rate.limit.tokens}") int rateLimitTokens) {
        this.authenticationService = authenticationService;

        Bandwidth bandwidth = Bandwidth.classic(rateLimitCapacity, Refill.greedy(rateLimitTokens, Duration.ofMinutes(rateLimitRefillTime)));
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

}
