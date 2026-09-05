package com.example.furkankarakas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the nested "address" object of a user:
 * { "city": "...", "geo": {...}, ... }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Address(String city, Geo geo) {
}
