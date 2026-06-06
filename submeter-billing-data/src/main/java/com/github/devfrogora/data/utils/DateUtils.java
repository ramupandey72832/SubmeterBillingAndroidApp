package com.github.devfrogora.data.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateUtils {

    // Define the strict format you want (e.g., yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd") // Note 'uuuu' instead of 'yyyy' for STRICT mode
                    .withResolverStyle(ResolverStyle.STRICT);

    public static String validateDate(String dateStr) {
        try {
            // This will parse AND validate the format/date mechanics simultaneously
            LocalDate verifiedDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            return dateStr;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd. Error: " + e.getMessage());
        }
    }

}
