package com.example.furkankarakas;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

import java.math.BigDecimal;

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
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void givenNullString_whenReversed_thenCorrect() {
        String input = null;
        String expected = "";
        String actual = StringUtils.reverse(input);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void givenString_whenCharacterArrayConverted_thenCorrect() {
        String input = "hello";
        assumeThat(input).isNotEmpty();

        Character[] expected = new Character[] { 'h', 'e', 'l', 'l', 'o' };
        Character[] actual = StringUtils.toCharacterArray(input);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void givenTwoBigDecimals_whenAdded_thenCorrect() {
        BigDecimal bd1 = BigDecimal.valueOf(0.1);
        BigDecimal bd2 = BigDecimal.valueOf(0.1);
        BigDecimal expected = new BigDecimal("0.200");
        BigDecimal actual = bd1.add(bd2);
        // Compares the big decimal by using the compareTo method from the Comparable
        // interface. Note that isEqualTo would have failed the test since the scales of
        // the numbers are different.
        assertThat(actual).isEqualByComparingTo(expected);
    }
}
