package com.intelligence.capture.risk;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PiiDetector {

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SSN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    private static final Pattern PHONE = Pattern.compile(
            "(?:\\+?\\d{1,3}[\\s-]?)?(?:\\(\\d{2,3}\\)|\\d{2,3})[\\s-]?\\d{3,4}[\\s-]?\\d{3,4}"
    );

    private static final Pattern CREDIT = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    private PiiDetector() {}

    public static Map<String, List<String>> find(String text) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        if (text == null || text.isBlank()) return out;

        addMatches(out, "email", EMAIL, text);
        addMatches(out, "ssn", SSN, text);
        addMatches(out, "phone", PHONE, text);
        addMatches(out, "credit_card", CREDIT, text);

        return out;
    }

    private static void addMatches(Map<String, List<String>> out, String key, Pattern p, String text) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            out.computeIfAbsent(key, k -> new ArrayList<>()).add(m.group().trim());
        }
    }
}
