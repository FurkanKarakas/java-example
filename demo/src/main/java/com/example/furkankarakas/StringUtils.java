package com.example.furkankarakas;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

public final class StringUtils {
    public static boolean isEmpty(String str) {
        return Optional.ofNullable(str).map(s -> s.trim()).filter(s -> !s.isEmpty()).isEmpty();
    }

    public static char[] toCharArray(String str) {
        return Optional.ofNullable(str).orElse("").toCharArray();
    }

    public static Character[] toCharacterArray(String str) {
        return Optional.ofNullable(str).orElse("").chars().mapToObj(c -> Character.valueOf((char) c))
                .toArray(Character[]::new);
    }

    public static List<Character> toCharacterList(String str) {
        return Optional.ofNullable(str).orElse("").chars().mapToObj(c -> Character.valueOf((char) c))
                .collect(Collectors.toList());
    }

    /**
     * We can use `Collections.reverse` on a Character List to reverse the list
     * in-place. An example how to convert a list of characters back to string:
     * characterList.stream().map(c -> c.toString()).collect(Collectors.joining());
     * We can use a simple for loop and apply string concatenation.
     * The easiest way, however, is with StringBuilder.
     * There is also StringBuffer, which is thread-safe.
     * 
     * @param str
     * @return
     */
    public static String reverse(String str) {
        return new StringBuilder().append(Optional.ofNullable(str).orElse("")).reverse().toString();
    }
}
