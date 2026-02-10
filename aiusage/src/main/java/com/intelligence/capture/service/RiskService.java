package com.intelligence.capture.service;

import com.intelligence.capture.model.PromptEvent;
import com.intelligence.capture.model.RiskScore;
import com.intelligence.capture.repo.PromptEventRepository;
import com.intelligence.capture.repo.RiskScoreRepository;
import com.intelligence.capture.risk.PiiDetector;
import com.intelligence.capture.risk.RiskScorer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RiskService {

    private final PromptEventRepository promptRepo;
    private final RiskScoreRepository riskRepo;

    public RiskService(PromptEventRepository promptRepo, RiskScoreRepository riskRepo) {
        this.promptRepo = promptRepo;
        this.riskRepo = riskRepo;
    }

    @Async
@Transactional
public void scoreTurn(UUID turnId) {
    try {
        PromptEvent e = promptRepo.findByTurnId(turnId).orElse(null);
        if (e == null) return;

        String prompt = e.getPrompt() == null ? "" : e.getPrompt();
        String response = e.getResponseText() == null ? "" : e.getResponseText();

        // If BOTH are empty, nothing to do
        if (prompt.isBlank() && response.isBlank()) return;

        RiskScorer.Result res = RiskScorer.score(prompt, response);

        RiskScore rs = new RiskScore();
        rs.setId(UUID.randomUUID());
        rs.setTurnId(turnId);
        rs.setScoredAt(Instant.now());
        rs.setScore(res.score());
        rs.setDetails(res.details());
        riskRepo.save(rs);

        // Update status based on score
        if ("HIGH".equals(res.level())) {
            e.setStatus("FLAGGED");
            // Optional: redact prompt+response; at least redact response
            e.setResponseText(redactResponse(response));
        } else if ("MEDIUM".equals(res.level())) {
            e.setStatus("REVIEW");
        } else {
            e.setStatus("CLEARED");
        }
        promptRepo.save(e);

    } catch (Exception ex) {
        // IMPORTANT: async failures often disappear silently otherwise
        System.err.println("[RiskService] scoreTurn failed for turnId=" + turnId + " err=" + ex.getMessage());
        ex.printStackTrace();
    }
}


    private String redactResponse(String responseText) {
        // Minimal redaction: replace detected strings with tags
        Map<String, List<String>> pii = PiiDetector.find(responseText);
        String out = responseText;

        for (var entry : pii.entrySet()) {
            String type = entry.getKey().toUpperCase();
            for (String token : entry.getValue()) {
                if (token != null && !token.isBlank()) {
                    out = out.replace(token, "[REDACTED:" + type + "]");
                }
            }
        }
        return out;
    }
}
