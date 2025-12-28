package com.example.demo.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author diwash
 * @created 12/20/25
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateFormat {
    public static final String DATE_FORMAT_DD_MMM_YYYY = "dd MMM yyyy";
    public static final String DATE_FORMAT_DD_MM_YYYY = "dd-MM-yyyy";
    public static final String DATE_FORMAT_DD_MMM_YYYY_UPPER = "ddMMMyyyy";
    public static final String DATE_FORMAT_MONTH_YEAR = "MMMM yyyy";
}
