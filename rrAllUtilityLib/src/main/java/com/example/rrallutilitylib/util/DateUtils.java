package com.example.rrallutilitylib.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

public final class DateUtils {

    private static final String DATE_PATTERN = "dd-MM-uuuu";
    private static final String DATE_FORMAT_MESSAGE = "dd-MM-yyyy";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN)
                    .withResolverStyle(ResolverStyle.STRICT);

    private DateUtils() {
        throw new UnsupportedOperationException(
                "DateUtils is a utility class"
        );
    }

    /**
     * Validates a date string in dd-MM-yyyy format.
     */
    public static String validateDate(String dateStr) {
        parseDate(dateStr);
        return dateStr;
    }

    /**
     * Parses a date string into LocalDate.
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException(
                    "Date cannot be null or blank."
            );
        }

        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date: '" + dateStr
                            + "'. Expected format: "
                            + DATE_FORMAT_MESSAGE
                            + " (for example, 16-10-2026).",
                    e
            );
        }
    }

    /**
     * Formats a LocalDate into dd-MM-yyyy.
     */
    public static String formatDate(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null.");
        return DATE_FORMATTER.format(date);
    }

    /**
     * Checks whether a date string is valid.
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }

        try {
            parseDate(dateStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns today's date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
}