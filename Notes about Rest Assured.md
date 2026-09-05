# Notes about Rest Assured

Talking to Copilot about Rest Assured framework.

## What is Rest Assured?

Rest Assured is a popular Java library for testing REST APIs. It simplifies
sending HTTP requests and validating responses with a fluent, BDD-style syntax
(given/when/then).

- Supports all HTTP methods (GET, POST, PUT, DELETE, PATCH...)
- Built-in JSON/XML path validation
- Works with Hamcrest matchers (`equalTo`, `hasSize`, `notNullValue`, ...)
- Integrates with JUnit 5, TestNG, Maven/Gradle

The fluent pattern is always the same:

- `given()` — set up: base URI, headers, content type, request body
- `when()` — the HTTP call: `get()`, `post()`, `put()`, `delete()`
- `then()` — assertions: `statusCode()`, `body()` with matchers

### Maven dependency

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>6.0.1</version>
    <scope>test</scope>
</dependency>
```

## Simple example with JSONPlaceholder

Test against the free fake API <https://jsonplaceholder.typicode.com>.
Full test class: `demo/src/test/java/com/example/furkankarakas/JsonPlaceholderTest.java`

### GET a single post

```java
@Test
public void getSinglePost_returns200AndExpectedFields() {
    given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/posts/1")
            .then()
            .statusCode(200)
            .body("userId", equalTo(1))
            .body("id", equalTo(1))
            .body("title", notNullValue());
}
```

### GET all posts (check array size)

```java
@Test
public void getAllPosts_returns100Posts() {
    given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/posts")
            .then()
            .statusCode(200)
            .body("$", hasSize(100)); // "$" = root of the JSON response
}
```

### POST a new post

```java
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
            .baseUri("https://jsonplaceholder.typicode.com")
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
```

### Run the tests

```powershell
mvn test -f "demo/pom.xml" -Dtest=JsonPlaceholderTest
```

All tests pass (verified 2026-09-05). Note: requires internet access.

## Testing nested structures

Rest Assured uses JsonPath / Groovy GPath syntax to navigate nested JSON.

### Path syntax cheat sheet

| Path syntax                     | Meaning                                    |
| ------------------------------- | ------------------------------------------ |
| `address.city`                  | Nested object field (dot notation)         |
| `address.geo.lat`               | Deeper nesting                             |
| `[0].title`                     | First element of an array                  |
| `find { it.id == 2 }.name`      | Find object in array by condition (GPath)  |
| `findAll { it.completed }`      | Filter: all matching elements              |
| `username` (on an array)        | Collects that field from every element     |
| `userId.unique()`               | Distinct values of a collected field       |

### Nested objects with dot notation

`GET /users/1` returns nested objects (`address`, `address.geo`, `company`):

```java
@Test
public void getUser_nestedObjectFields_withDotNotation() {
    given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/users/1")
            .then()
            .statusCode(200)
            .body("address.city", equalTo("Gwenborough"))
            .body("address.geo.lat", startsWith("-37"))
            .body("company.name", containsString("Romaguera"));
}
```

### Arrays of objects: find / collect / everyItem

`GET /users` returns an array of 10 users:

```java
@Test
public void getUsers_arrayOfObjects_findByCondition() {
    given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/users")
            .then()
            .statusCode(200)
            .body("$", hasSize(10))
            .body("find { it.id == 3 }.username", equalTo("Samantha"))
            .body("username", hasItem("Bret"))       // collect field from all elements
            .body("id", everyItem(notNullValue()));  // every element has an id
}
```

### Array indexing, filtering and query params

`GET /todos?userId=1` returns 20 todos of one user:

```java
@Test
public void getTodosOfUser_filterAndCheckNestedFields() {
    given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .queryParam("userId", 1)
            .when()
            .get("/todos")
            .then()
            .statusCode(200)
            .body("$", hasSize(20))
            .body("[0].userId", equalTo(1))
            .body("findAll { it.completed }.size()", equalTo(11))
            .body("userId.unique()", equalTo(java.util.List.of(1)));
}
```

Key takeaways:

1. Dot notation walks into nested objects: `address.city`, `company.name`.
2. `[i]` indexes arrays: `[0].userId`.
3. GPath queries arrays: `find` (first match), `findAll` (all matches),
   plain field name collects that field from every element.
4. All Hamcrest matchers work on the result (`everyItem`, `hasItem`,
   `containsString`, ...).
