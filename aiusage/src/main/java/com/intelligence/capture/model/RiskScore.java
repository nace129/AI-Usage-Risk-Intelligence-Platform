package com.intelligence.capture.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "risk_scores")
public class RiskScore {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "turn_id", nullable = false)
    private UUID turnId;

    @Column(name = "scored_at", nullable = false)
    private Instant scoredAt;

    @Column(nullable = false)
    private double score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> details = new LinkedHashMap<>();

    // ---- getters/setters ----
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTurnId() { return turnId; }
    public void setTurnId(UUID turnId) { this.turnId = turnId; }

    public Instant getScoredAt() { return scoredAt; }
    public void setScoredAt(Instant scoredAt) { this.scoredAt = scoredAt; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) {
        this.details = (details == null) ? new LinkedHashMap<>() : details;
    }
}
