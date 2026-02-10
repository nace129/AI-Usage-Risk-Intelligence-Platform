package com.intelligence.capture.risk;

import java.util.*;

public final class RiskScorer {

    public record Result(double score, String level, Map<String, Object> details) {}

    private RiskScorer() {}

    public static Result score(String prompt, String response) {
        String p = (prompt == null) ? "" : prompt;
        String r = (response == null) ? "" : response;
        String text = (p + "\n" + r).trim();

        Map<String, List<String>> pii = PiiDetector.find(text);

        double score = 0.05; // baseline
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("pii", pii);

        if (pii.containsKey("credit_card")) score += 0.40;
        if (pii.containsKey("ssn")) score += 0.35;
        if (pii.containsKey("email")) score += 0.20;
        if (pii.containsKey("phone")) score += 0.10;

        // secret keywords
        String low = text.toLowerCase();
        List<String> secretWords = List.of("password", "passwd", "secret", "api key", "apikey", "token", "private key");
        int hits = 0;
        for (String w : secretWords) if (low.contains(w)) hits++;
        score += Math.min(0.15, hits * 0.05);
        details.put("secret_word_hits", hits);

        // clamp
        score = Math.min(1.0, score);

        String level =
                (score >= 0.70) ? "HIGH" :
                (score >= 0.40) ? "MEDIUM" : "LOW";

        details.put("score", score);
        details.put("level", level);

        details.put("debug_prompt_len", p.length());
        details.put("debug_response_len", r.length());
        
        return new Result(score, level, details);

        

    }
}
