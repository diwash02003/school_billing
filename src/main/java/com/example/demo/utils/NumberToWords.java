package com.example.demo.utils;

import org.springframework.stereotype.Component;

/**
 * @author diwash
 * @created 12/20/25
 */

@Component
public class NumberToWords {


    // Constants
    public static final String DEFAULT_AMOUNT_PLACEHOLDER = "______";
    public static final String ZERO_RUPEES_ONLY = "zero rupees only";
    public static final String RUPEES = "rupees";
    public static final String RUPEE = "rupee";
    public static final String PAISE = "paise";
    public static final String AND = "and";
    public static final String ONLY = "only";


    private static final String[] UNITS = {
            "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
            "eighteen", "nineteen"
    };

    private static final String[] TENS = {
            "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    };

    /**
     * Convert a number to words
     *
     * @param n the number to convert
     * @return the number in words
     */
    public static String convert(long n) {
        if (n == 0) return "zero";
        if (n < 0) return "minus " + convert(-n);

        StringBuilder words = new StringBuilder();

        // Crores (10^7)
        if ((n / 10000000) > 0) {
            words.append(convert(n / 10000000)).append(" crore ");
            n %= 10000000;
        }

        // Lakhs (10^5)
        if ((n / 100000) > 0) {
            words.append(convert(n / 100000)).append(" lakh ");
            n %= 100000;
        }

        // Thousands
        if ((n / 1000) > 0) {
            words.append(convert(n / 1000)).append(" thousand ");
            n %= 1000;
        }

        // Hundreds
        if ((n / 100) > 0) {
            words.append(convert(n / 100)).append(" hundred ");
            n %= 100;
        }

        // Tens and Units
        if (n > 0) {
            if (!words.isEmpty()) words.append("and ");

            if (n < 20) {
                words.append(UNITS[(int) n]);
            } else {
                words.append(TENS[(int) (n / 10)]);
                if ((n % 10) > 0) {
                    words.append(" ").append(UNITS[(int) (n % 10)]);
                }
            }
        }

        return words.toString().trim();
    }

    /**
     * Convert a monetary amount (Double) to words with rupees and paise
     *
     * @param amount the amount to convert
     * @return amount in words (e.g., "one thousand two hundred thirty-four rupees and fifty-six paise only")
     */
    public static String convertAmountToWords(Double amount) {
        if (amount == null) return DEFAULT_AMOUNT_PLACEHOLDER;

        try {
            long rupees = amount.longValue();
            long paise = Math.round((amount - rupees) * 100);

            String rupeesWords = convert(rupees);
            StringBuilder result = new StringBuilder();

            if (rupees > 0) {
                result.append(rupeesWords).append(" ").append(rupees == 1 ? RUPEE : RUPEES);
            }

            if (paise > 0) {
                if (rupees > 0) result.append(" ").append(AND).append(" ");
                String paiseWords = convert(paise);
                result.append(paiseWords).append(" ").append(PAISE);
            }

            if (!result.isEmpty()) {
                result.append(" ").append(ONLY);
            } else {
                result.append(ZERO_RUPEES_ONLY);
            }

            return capitalizeFirstLetter(result.toString());

        } catch (Exception e) {
            return String.format("%,.2f", amount) + " " + RUPEES + " " + ONLY;
        }
    }

    /**
     * Capitalize the first letter of the words string
     */
    private static String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Convert a string representation of number to words
     *
     * @param numberStr the number as string
     * @return number in words
     */
    public static String convert(String numberStr) {
        try {
            if (numberStr == null || numberStr.trim().isEmpty()) return "";

            // Remove commas and whitespace
            String cleanStr = numberStr.replaceAll("[,\\s]", "");

            if (cleanStr.contains(".")) {
                return convertAmountToWords(Double.parseDouble(cleanStr));
            } else {
                return convert(Long.parseLong(cleanStr));
            }
        } catch (NumberFormatException e) {
            return numberStr; // Return original if can't parse
        }
    }
}