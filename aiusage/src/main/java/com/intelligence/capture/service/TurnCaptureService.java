package com.intelligence.capture.service;

import com.intelligence.capture.dto.CapturePromptRequest;
import com.intelligence.capture.dto.CaptureResponseRequest;
import com.intelligence.capture.model.PromptEvent;
import com.intelligence.capture.repo.PromptEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TurnCaptureService {

    public static final String STATUS_PROMPT_ONLY = "PROMPT_ONLY";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private final PromptEventRepository repo;
    private final RiskService riskService;
    

    public TurnCaptureService(PromptEventRepository repo, RiskService riskService) {
        this.repo = repo;
        this.riskService = riskService;
    }

    @Transactional
    public PromptEvent capturePrompt(CapturePromptRequest req) {
        UUID turnId = parseUuid(req.turnId);

        // Idempotency: if extension retries, return existing row
        Optional<PromptEvent> existing = repo.findByTurnId(turnId);
        if (existing.isPresent()) return existing.get();

        String prompt = req.prompt.trim();

        PromptEvent e = new PromptEvent();
        e.setId(UUID.randomUUID());
        e.setTurnId(turnId);

        e.setPrompt(prompt);
        e.setCapturedAt(parseInstantOrNow(req.capturedAt));

        e.setPageUrl(nullIfBlank(req.pageUrl));
        e.setUserAgent(nullIfBlank(req.userAgent));
        e.setDeviceId(nullIfBlank(req.deviceId));
        e.setExtensionVersion(nullIfBlank(req.extensionVersion));
        e.setSendMethod(normalizeSendMethod(req.sendMethod));

        e.setPromptLength(prompt.length());
        e.setPromptHash(sha256Hex(prompt));

        Map<String, Object> meta = new HashMap<>();
        meta.put("source", "chatgpt.com");
        meta.put("pageHost", safeHostHint(req.pageUrl));
        meta.put("ingest", "springboot");
        e.setMetadata(meta); // NOT NULL

        e.setStatus(STATUS_PROMPT_ONLY);

        // response_metadata is NOT NULL in DB; ensure it’s never null
        e.setResponseMetadata(new HashMap<>());

        e.setCreatedAt(Instant.now());
        
        PromptEvent saved = repo.save(e);
riskService.scoreTurn(saved.getTurnId()); // score prompt immediately
return saved;

        //return repo.save(e);
    }

    @Transactional
    public PromptEvent captureResponse(CaptureResponseRequest req) {
        UUID turnId = parseUuid(req.turnId);

        PromptEvent e = repo.findByTurnId(turnId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown turnId: " + turnId));

        // Idempotency: if response already captured, don’t overwrite
        if (STATUS_COMPLETED.equals(e.getStatus()) && e.getResponseText() != null && !e.getResponseText().isBlank()) {
            return e;
        }

        String response = req.responseText.trim();

        e.setResponseText(response);
        e.setResponseCapturedAt(parseInstantOrNow(req.responseCapturedAt));
        e.setResponseLength(response.length());
        e.setResponseHash(sha256Hex(response));

        Map<String, Object> rmeta = new HashMap<>();
        if (req.modelHint != null && !req.modelHint.isBlank()) {
            rmeta.put("modelHint", req.modelHint.trim());
        }
        e.setResponseMetadata(rmeta); // NOT NULL

        e.setStatus(STATUS_COMPLETED);

        PromptEvent saved = repo.save(e);

        // Kick off Phase 3 async risk scoring
        riskService.scoreTurn(saved.getTurnId());

        return saved;
    }

    // ---------- helpers ----------
    private UUID parseUuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UUID: " + s);
        }
    }

    private String normalizeSendMethod(String s) {
        if (s == null) return "unknown";
        s = s.trim().toLowerCase();
        return (s.equals("enter") || s.equals("button")) ? s : "unknown";
    }

    private Instant parseInstantOrNow(String iso) {
        if (iso == null || iso.isBlank()) return Instant.now();
        try { return Instant.parse(iso); } catch (Exception ignored) { return Instant.now(); }
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return null; // don’t block ingestion
        }
    }

    private String safeHostHint(String url) {
        if (url == null) return null;
        try {
            String u = url.trim();
            int scheme = u.indexOf("://");
            if (scheme >= 0) u = u.substring(scheme + 3);
            int slash = u.indexOf('/');
            if (slash >= 0) u = u.substring(0, slash);
            return u.length() > 200 ? u.substring(0, 200) : u;
        } catch (Exception e) {
            return null;
        }
    }
}
