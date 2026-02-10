package com.intelligence.capture.repo;

import com.intelligence.capture.model.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {

    Optional<RiskScore> findTop1ByTurnIdOrderByScoredAtDesc(UUID turnId);

    List<RiskScore> findTop50ByScoreGreaterThanEqualOrderByScoredAtDesc(double minScore);
}
