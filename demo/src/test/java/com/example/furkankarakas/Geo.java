package com.example.furkankarakas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps "address.geo": { "lat": "-37.3159", "lng": "81.1496" }.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Geo(String lat, String lng) {
}
