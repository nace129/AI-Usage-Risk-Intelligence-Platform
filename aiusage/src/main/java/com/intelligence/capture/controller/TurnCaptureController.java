package com.intelligence.capture.controller;

import com.intelligence.capture.dto.CapturePromptRequest;
import com.intelligence.capture.dto.CaptureResponseRequest;
import com.intelligence.capture.model.PromptEvent;
import com.intelligence.capture.model.RiskScore;
import com.intelligence.capture.repo.PromptEventRepository;
import com.intelligence.capture.repo.RiskScoreRepository;
import com.intelligence.capture.service.TurnCaptureService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/turns")
public class TurnCaptureController {

    private final TurnCaptureService service;
    private final PromptEventRepository repo;
    private final RiskScoreRepository riskRepo;

    public TurnCaptureController(TurnCaptureService service, PromptEventRepository repo, RiskScoreRepository riskRepo) {
        this.service = service;
        this.repo = repo;
        this.riskRepo = riskRepo;
    }

    @PostMapping("/prompt")
    public ResponseEntity<?> capturePrompt(@Valid @RequestBody CapturePromptRequest req) {
        PromptEvent saved = service.capturePrompt(req);
        return ResponseEntity.ok(new PromptAck(saved.getTurnId(), saved.getId(), saved.getStatus(), saved.getCapturedAt()));
    }

    @PostMapping("/response")
    public ResponseEntity<?> captureResponse(@Valid @RequestBody CaptureResponseRequest req) {
        PromptEvent saved = service.captureResponse(req);
        return ResponseEntity.ok(new ResponseAck(saved.getTurnId(), saved.getId(), saved.getStatus(), saved.getResponseCapturedAt()));
    }

    @GetMapping("/recent")
    public List<PromptEvent> recent(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "deviceId", required = false) String deviceId
    ) {
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            return repo.findTop50ByDeviceIdOrderByCapturedAtDesc(deviceId.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            return repo.findTop50ByStatusOrderByCapturedAtDesc(status.trim().toUpperCase());
        }
        return repo.findTop50ByOrderByCapturedAtDesc();
    }

    public record PromptAck(UUID turnId, UUID id, String status, Instant capturedAt) {}
    public record ResponseAck(UUID turnId, UUID id, String status, Instant responseCapturedAt) {}

    @GetMapping("/{turnId}/risk")
public RiskScore risk(@PathVariable String turnId) {
    UUID id = UUID.fromString(turnId);
    return riskRepo.findTop1ByTurnIdOrderByScoredAtDesc(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No risk score found for turnId=" + turnId + " (risk job may not have run yet)"));
}


    @GetMapping("/risk/recent")
    public List<RiskScore> recentRisk(@RequestParam(name="minScore", defaultValue="0.4") double minScore) {
        return riskRepo.findTop50ByScoreGreaterThanEqualOrderByScoredAtDesc(minScore);
    }


}
