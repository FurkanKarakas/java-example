package com.example.furkankarakas;

public final class IntegerUtils {
    public static int reverseFirstAndLastDigits(int input) {
        int sign = Integer.signum(input);
        input = Math.abs(input);
        int result = input;
        int firstDigit = result % 10;
        int exponent = 0;
        while (input / 10 != 0) {
            exponent++;
            input /= 10;
        }
        int lastDigit = input;
        result = result - firstDigit - lastDigit * Math.powExact(10, exponent);
        result = result + lastDigit + firstDigit * Math.powExact(10, exponent);
        return result * sign;
    }
}
