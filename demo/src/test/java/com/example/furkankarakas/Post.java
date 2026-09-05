package com.example.furkankarakas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record matching JSONPlaceholder's /posts resource:
 * { "userId": 1, "id": 1, "title": "...", "body": "..." }
 *
 * Jackson maps JSON fields to the record components by name.
 * ignoreUnknown = true -> JSON fields with no matching component are ignored.
 * 
 * @JsonProperty -> use when the Java component name should differ from the
 *               JSON field name (here: JSON "body" is exposed as content()).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Post(int userId, int id, String title, @JsonProperty("body") String content) {
}
