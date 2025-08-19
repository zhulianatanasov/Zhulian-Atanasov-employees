package com.sirma.utils;

import java.util.Objects;

public class CommonUtils {

    public static Character separatorToDelimiter(final Character separator) {
        return switch (separator) {
            case 'C' -> ',';
            case 'S' -> ';';
            case 'T' -> '\t';
            case 'P' -> '|';
            default -> throw new IllegalStateException("Unexpected value: " + separator);
        };
    }

    public static String trim(final String text) {
        return Objects.nonNull(text) ? text.trim() : null;
    }
}
