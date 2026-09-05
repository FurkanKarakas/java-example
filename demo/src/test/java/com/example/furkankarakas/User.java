package com.example.furkankarakas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Record matching JSONPlaceholder's /users resource (partial mapping).
 * ignoreUnknown = true -> we only model the fields we need; the rest
 * of the JSON (phone, website, company, ...) is ignored.
 * Nested objects map to the separate Address and Geo records.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(int id, String name, String username, String email, Address address) {
}
