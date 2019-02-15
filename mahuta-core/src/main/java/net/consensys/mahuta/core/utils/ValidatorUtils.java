package net.consensys.mahuta.core.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class ValidatorUtils {

    private ValidatorUtils() { }

    public static boolean isNull(Object input) {
        return Objects.isNull(input);
    }

    public static boolean isEmpty(String input) {
        return isNull(input) || input.isEmpty();
    }

    public static boolean isEmpty(Collection<?> input) {
        return isNull(input) || input.isEmpty();
    }

    public static boolean isEmpty(String... input) {
        return isNull(input) || input.length == 0;
    }

    public static void rejectIfEmpty(String field, String value) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
    }

    public static void rejectIfEmpty(String field, String value, String message) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(field + " cannot be empty. " + message);
        }
    }

    public static void rejectIfNull(String field, Object value) {
        if (isNull(value)) {
            throw new IllegalArgumentException(field + " cannot be null.");
        }
    }

    public static void rejectIfNull(String field, Object value, String message) {
        if (isNull(value)) {
            throw new IllegalArgumentException(field + " cannot be null. " + message);
        }
    }

    public static void rejectIfNegative(String field, Integer value) {
        if (isNull(value) || value < 0) {
            throw new IllegalArgumentException(field + " should not be negative or null.");
        }
    }

    public static void rejectIfEmpty(String field, String... value) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(field + " should not be empty or null.");
        }
    }

    public static void rejectIfEmpty(String field, Collection<?> value) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(field + " should not be empty or null.");
        }
    }

    public static <T> void rejectIfDifferentThan(String field, T value, T... expectedValues) {
        Stream.of(expectedValues)
            .filter(expected -> value.equals(expected))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(field + " has an incorrect value ("+value+"). Expected: " + expectedValues));
    }

}
