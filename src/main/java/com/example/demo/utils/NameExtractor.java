package com.example.demo.utils;

import com.example.demo.constants.DateFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author diwash
 * @created 12/20/25
 */

@Component
public class NameExtractor {
    public String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown";
        }
        return fullName.split(" ")[0];
    }

    public String formatDateForFileName(LocalDate date) {
        if (date == null) {
            return "NA";
        }
        return date.format(DateTimeFormatter.ofPattern(DateFormat.DATE_FORMAT_DD_MMM_YYYY_UPPER));
    }

    public String formatMonthsForFileName(List<String> months) {
        if (months == null || months.isEmpty()) {
            return "NA";
        }
        return String.join("_", months);
    }
}
