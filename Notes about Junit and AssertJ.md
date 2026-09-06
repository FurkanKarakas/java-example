# Notes about JUnit and AssertJ

## Popular JUnit 5 (Jupiter) Annotations

### Lifecycle

- `@Test` — marks a method as a test
- `@BeforeEach` / `@AfterEach` — run before/after every test method
- `@BeforeAll` / `@AfterAll` — run once per class (must be `static`)

### Execution Control

- `@Disabled("reason")` — skips a test or class
- `@Order(n)` + `@TestMethodOrder(OrderAnnotation.class)` — control test execution order
- `@Timeout(seconds)` — fails the test if it exceeds the time limit

### Parameterization & Repetition

- `@ParameterizedTest` — run a test with multiple inputs, combined with:
  - `@ValueSource` — simple literals (strings, ints, etc.)
  - `@CsvSource` — comma-separated value sets
  - `@MethodSource` — values supplied by a factory method
  - `@EnumSource` — enum constants
- `@RepeatedTest(n)` — run a test n times
- `@TestFactory` — dynamic tests generated at runtime

### Organization & Metadata

- `@DisplayName("...")` — readable test names in reports
- `@Nested` — group related tests in inner classes
- `@Tag("slow")` — label and filter tests when running

### Other Useful Annotations

- `@TestInstance(Lifecycle.PER_CLASS)` — one test instance for all methods (allows non-static `@BeforeAll`)
- `@ExtendWith(MockitoExtension.class)` — register extensions (e.g., Mockito)
- `@TempDir` — inject a temporary directory for file-based tests

### JUnit 4 Equivalents (for reference)

| JUnit 5 | JUnit 4 |
| --- | --- |
| `@BeforeEach` / `@AfterEach` | `@Before` / `@After` |
| `@BeforeAll` / `@AfterAll` | `@BeforeClass` / `@AfterClass` |
| `@Disabled` | `@Ignore` |
| `@ExtendWith` | `@RunWith` |
| `@Tag` | `@Category` |

## Example

```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class StringUtilsTest {

    @BeforeAll
    static void initAll() {
        // runs once before all tests
    }

    @BeforeEach
    void init() {
        // runs before each test
    }

    @Test
    @DisplayName("Reversing a string works")
    @Order(1)
    void testReverse() {
        assertEquals("cba", StringUtils.reverse("abc"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"racecar", "level", "madam"})
    @DisplayName("Palindromes are detected")
    void testPalindromes(String word) {
        assertTrue(StringUtils.isPalindrome(word));
    }

    @Test
    @Disabled("Waiting for bug fix #42")
    void testNotReadyYet() {
        // skipped
    }
}
```

## @CsvSource Example

Each line in a `@CsvSource` is one test invocation; values are split by comma and converted to the parameter types automatically.

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
        "1, 1, 2",
        "2, 3, 5",
        "-1, 1, 0",
        "0, 0, 0"
    })
    void testAdd(int a, int b, int expected) {
        assertEquals(expected, a + b);
    }

    // Strings, booleans and empty values are converted too
    @ParameterizedTest
    @CsvSource({
        "apple,  true",
        "'',     true",   // empty string (quoted)
        "dog,    false"
    })
    void testIsShortWord(String word, boolean expected) {
        assertEquals(expected, word.length() <= 5);
    }

    // Custom delimiter and null values
    @ParameterizedTest
    @CsvSource(value = {"one | 1", "N/A | null"}, delimiterString = "|", nullValues = "null")
    void testCustomDelimiter(String word, Integer number) {
        // "N/A | null" passes a real null for the second argument
    }
}
```

- `name = "{0} + {1} = {2}"` — placeholders `{0}`, `{1}`, ... are replaced with the argument values in test reports
- Use `delimiter = ';'` or `delimiterString = "|"` when values contain commas
- Use `nullValues = "N/A"` to map a literal to `null`
- Use single quotes (`''`) for empty strings

## AssertJ Quick Reference

- `assertThat(actual).isEqualTo(expected)`
- `assertThat(string).startsWith("foo").endsWith("bar").contains("oo")`
- `assertThat(list).hasSize(3).contains("a", "b").doesNotContain("z")`
- `assertThatThrownBy(() -> ...).isInstanceOf(IllegalArgumentException.class)`
- `assertThatCode(() -> ...).doesNotThrowAnyException()`
- `assertThat(optional).isPresent().contains(value)`

## Assertions vs Assumptions

**Assertions** verify expected behavior — a failure **fails** the test:

```java
assertThat(user.getName()).isEqualTo("Furkan");
```

**Assumptions** verify preconditions — a failure **skips** (aborts) the test instead of failing it. Use them for environment-dependent tests:

```java
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Test
void onlyOnCI() {
    assumeThat(System.getenv("CI")).isEqualTo("true"); // AssertJ style
    assumeTrue(System.getProperty("os.name").contains("Linux")); // JUnit style
    // rest of the test runs only if assumptions hold
}
```

In reports, an aborted test shows as **skipped**, not failed.

### Soft Assertions

Collect all failures instead of stopping at the first one:

```java
SoftAssertions softly = new SoftAssertions();
softly.assertThat(user.getName()).isEqualTo("Furkan");
softly.assertThat(user.getAge()).isGreaterThan(18);
softly.assertAll(); // all failures reported together here
```

### Similar / Related Concepts

| Concept | Library | Behavior |
| --- | --- | --- |
| `assumeTrue` / `assumeThat` | JUnit 5 (`Assumptions`) | Aborts test when condition is false |
| `Assume.assumeThat` | JUnit 4 | Older equivalent |
| `SoftAssertions` | AssertJ, Truth | Failures aggregated, reported at the end |
| `fail()` / `fail(String)` | AssertJ, JUnit | Unconditional failure (useful in `catch` blocks) |
| `Preconditions.checkArgument` | Guava | *Production* code validation — throws exception, not a test concept |
| `then(...)` (BDD style) | AssertJ (`BDDAssertions`) | Same assertions, reads better in Given/When/Then flows |
| Hamcrest matchers | Hamcrest | Alternative assertion style: `assertThat(x, is(5))` |
| Truth | Google | Another fluent assertion library with the same idea |

**Rule of thumb:** assertions answer *"did the code do the right thing?"* — assumptions answer *"is this test even applicable right now?"*
