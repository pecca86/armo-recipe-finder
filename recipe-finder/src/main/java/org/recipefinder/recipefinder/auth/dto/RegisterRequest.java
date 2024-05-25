package org.recipefinder.recipefinder.auth.dto;

// TODO Check if Spring Auth can work with record...
public record RegisterRequest(String firstName, String lastName, String email, String password) {
}
