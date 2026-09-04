package com.example.furkankarakas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 * Writing tests in JUnit: https://docs.junit.org/6.1.3/writing-tests/intro.html
 * AssertJ: https://assertj.github.io/doc/
 * Selenium docs: https://www.selenium.dev/documentation/
 * Selenium PageFactory:
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/PageFactory.html
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */

    @Test
    public void givenNonNullString_whenReversed_thenCorrect() {
        String input = "hello";
        String expected = "olleh";
        String actual = StringUtils.reverse(input);
        assertTrue(expected.equals(actual));
    }

    @Test
    public void givenNullString_whenReversed_thenCorrect() {
        String input = null;
        String expected = "";
        String actual = StringUtils.reverse(input);
        assertTrue(expected.equals(actual));
    }

    @Test
    public void givenString_whenCharacterArrayConverted_thenCorrect() {
        String input = "hello";
        Character[] expected = new Character[] { 'h', 'e', 'l', 'l', 'o' };
        Character[] actual = StringUtils.toCharacterArray(input);
        assertArrayEquals(expected, actual);
    }
}
