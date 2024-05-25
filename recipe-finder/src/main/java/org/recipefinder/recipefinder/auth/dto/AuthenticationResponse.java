package org.recipefinder.recipefinder.auth.dto;

import org.springframework.http.HttpStatus;

public record AuthenticationResponse(int statusCode, String message, String token) {
}
