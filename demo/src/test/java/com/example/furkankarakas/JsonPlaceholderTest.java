package com.example.furkankarakas;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

/**
 * Simple Rest Assured examples against the free fake API:
 * https://jsonplaceholder.typicode.com
 */
public class JsonPlaceholderTest {

    private static final String BASE_URI = "https://jsonplaceholder.typicode.com";

    @Test
    public void getSinglePost_returns200AndExpectedFields() {
        given()
                .baseUri(BASE_URI)
                .when()
                .get("/posts/1")
                .then()
                .statusCode(200)
                .body("userId", equalTo(1))
                .body("id", equalTo(1))
                .body("title", notNullValue());
    }

    @Test
    public void getAllPosts_returns100Posts() {
        given()
                .baseUri(BASE_URI)
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .body("$", hasSize(100));
    }

    @Test
    public void createPost_returns201AndEchoedData() {
        String requestBody = """
                {
                  "title": "hello",
                  "body": "from Rest Assured",
                  "userId": 1
                }
                """;

        given()
                .baseUri(BASE_URI)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo("hello"))
                .body("userId", equalTo(1))
                .body("id", notNullValue());
    }

    // ------------------------------------------------------------------
    // Nested structures: use dot notation for objects, [i] for arrays,
    // and Groovy GPath (find / findAll / collect) to query arrays.
    // ------------------------------------------------------------------

    @Test
    public void getUser_nestedObjectFields_withDotNotation() {
        // GET /users/1 -> { "address": { "city": "Gwenborough",
        // "geo": { "lat": "-37.3159", ... } }, ... }
        given()
                .baseUri(BASE_URI)
                .when()
                .get("/users/1")
                .then()
                .statusCode(200)
                .body("address.city", equalTo("Gwenborough"))
                .body("address.geo.lat", startsWith("-37"))
                .body("company.name", containsString("Romaguera"));
    }

    @Test
    public void getUsers_arrayOfObjects_findByCondition() {
        // GET /users -> array of user objects.
        // "find { ... }" picks one element; "*.field" collects a field from all.
        given()
                .baseUri(BASE_URI)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("$", hasSize(10))
                .body("find { it.id == 3 }.username", equalTo("Samantha"))
                .body("username", hasItem("Bret")) // collect all usernames, check one
                .body("id", everyItem(notNullValue())); // every element has an id
    }

    @Test
    public void getTodosOfUser_filterAndCheckNestedFields() {
        // Query param + array indexing: first todo of user 1
        given()
                .baseUri(BASE_URI)
                .queryParam("userId", 1)
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("$", hasSize(20))
                .body("[0].userId", equalTo(1))
                .body("findAll { it.completed }.size()", equalTo(11))
                .body("userId.unique()", equalTo(List.of(1))); // all belong to user 1
    }
}
