# Notes about Rest Assured

Talking to Copilot about Rest Assured framework.

Complete user documentation: <https://github.com/rest-assured/rest-assured/wiki/Usage>

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

## What does `$` mean?

`$` is the **root node** of the response document — it does not count how
many JSON arrays exist. `body("$", hasSize(100))` means "the root node (the
array itself) has 100 elements".

| Path       | Resolves to              | `hasSize(...)` checks        |
| ---------- | ------------------------ | ---------------------------- |
| `$`        | The root — the array     | Size of the array (100)      |
| `$[0]`     | First element (object)   | Number of fields in it       |
| `$[0].id`  | A scalar `1`             | Size makes no sense here     |

If the array were wrapped in an object (`{ "posts": [...], "total": 100 }`):

- `body("$", hasSize(2))` — root object has 2 fields
- `body("posts", hasSize(100))` — the nested array
- `body("total", equalTo(100))` — a scalar field

The "automatic extraction" for collections comes from Groovy GPath: a plain
field name on an array (`body("id", ...)`) is evaluated as "collect `id` from
every element" — no explicit loop needed.

## Deserializing JSON into POJOs / records

Rest Assured 5+ no longer bundles a JSON mapper — add Jackson Databind
explicitly:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.20.0</version>
    <scope>test</scope>
</dependency>
```

Without it you get:
`IllegalStateException: Cannot parse object because no JSON deserializer
found in classpath.`

### The records

Records are ideal for API response DTOs (Data Transfer Object): immutable, one line per type,
Jackson 2.12+ supports them natively. One file per record (same package,
no imports needed between them):

```java
// Post.java — @JsonProperty renames a component (JSON "body" -> content())
@JsonIgnoreProperties(ignoreUnknown = true)
public record Post(int userId, int id, String title,
                   @JsonProperty("body") String content) {
}

// User.java
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(int id, String name, String username, String email,
                   Address address) {
}

// Address.java
@JsonIgnoreProperties(ignoreUnknown = true)
public record Address(String city, Geo geo) {
}

// Geo.java
@JsonIgnoreProperties(ignoreUnknown = true)
public record Geo(String lat, String lng) {
}
```

Files: `Post.java`, `User.java`, `Address.java`, `Geo.java` in
`demo/src/test/java/com/example/furkankarakas/`.

### Deserialization tests

```java
import io.restassured.common.mapper.TypeRef;

@Test
public void getPost_deserializedToPojo() {
    // .extract().as(Post.class) converts the JSON body into a Post record
    Post post = given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/posts/1")
            .then()
            .statusCode(200)
            .extract()
            .as(Post.class);

    assertEquals(1, post.userId());
    assertEquals(1, post.id());
    assertTrue(post.title().startsWith("sunt aut facere"));
    assertTrue(post.content().startsWith("quia et suscipit")); // JSON "body"
}

@Test
public void getAllPosts_deserializedToListOfPojos() {
    // Arrays need TypeRef so generics survive type erasure
    List<Post> posts = given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/posts")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<List<Post>>() {
            });

    assertEquals(100, posts.size());
    assertTrue(posts.stream().allMatch(p -> p.id() > 0));
}

@Test
public void getUser_deserializedWithNestedPojos() {
    // Nested JSON objects map to nested record fields automatically
    User user = given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .when()
            .get("/users/1")
            .then()
            .statusCode(200)
            .extract()
            .as(User.class);

    assertEquals(1, user.id());
    assertEquals("Gwenborough", user.address().city());
    assertTrue(user.address().geo().lat().startsWith("-37"));
}

@Test
public void createPost_serializedFromRecord() {
    // Serialization works the other way too: pass a record as the body
    Post newPost = new Post(1, 0, "serialized from record",
            "Jackson converts this to JSON");

    Post created = given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .contentType("application/json")
            .body(newPost) // record -> JSON request body
            .when()
            .post("/posts")
            .then()
            .statusCode(201)
            .extract()
            .as(Post.class);

    assertEquals(101, created.id()); // JSONPlaceholder returns id 101
    assertEquals("serialized from record", created.title());
}
```

### Key rules for (de)serialization

1. `.extract().as(Class)` pulls the body out of the validation chain and
   converts it; afterwards use plain JUnit assertions on the object.
2. Arrays need `TypeRef`: `.as(new TypeRef<List<Post>>() {})` — Java
   generics are erased at runtime, the anonymous subclass preserves them.
3. Classic POJOs need a no-args constructor + getters/setters; records just
   need components — Jackson calls the canonical constructor.
4. `@JsonIgnoreProperties(ignoreUnknown = true)` is essential for partial
   mapping. Without it Jackson throws `UnrecognizedPropertyException` for
   any JSON field not modeled in the record.
5. `@JsonProperty("jsonName")` on a record component renames the mapping
   (e.g. JSON `"body"` -> `content()`). Works in both directions. Useful
   for snake_case APIs or Java keyword conflicts. Alternative: set a global
   `PropertyNamingStrategies.SNAKE_CASE` via `ObjectMapperConfig`.
6. Nested JSON maps automatically through the type chain:
   `user.address().geo().lat()`.
7. Records can be nested inside each other or live in separate files —
   purely organizational. Separate files make types reusable across DTOs.
8. After restructuring classes, run `mvn clean test` — stale compiled
   classes (e.g. an old `User$Address.class`) can shadow new top-level
   types and cause confusing `InvalidDefinitionException` errors.
