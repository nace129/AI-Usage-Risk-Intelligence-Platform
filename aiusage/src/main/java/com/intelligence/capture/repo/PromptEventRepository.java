package com.intelligence.capture.repo;

import com.intelligence.capture.model.PromptEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromptEventRepository extends JpaRepository<PromptEvent, UUID> {
    List<PromptEvent> findTop50ByOrderByCapturedAtDesc();
    List<PromptEvent> findTop50ByDeviceIdOrderByCapturedAtDesc(String deviceId);
    
    Optional<PromptEvent> findByTurnId(UUID turnId);
    List<PromptEvent> findTop50ByStatusOrderByCapturedAtDesc(String status);

    // Optimized partial update — updates only response fields and status
    // @Modifying
    // @Query("""
    //     UPDATE PromptEvent p
    //     SET p.responseText = :responseText,
    //         p.responseHash = :responseHash,
    //         p.responseAt = :responseAt,
    //         p.responseMetadata = :responseMetadata,
    //         p.status = :status
    //     WHERE p.turnId = :turnId
    // """)
    // int updateResponseByTurnId(
    //         @Param("turnId") UUID turnId,
    //         @Param("responseText") String responseText,
    //         @Param("responseHash") String responseHash,
    //         @Param("responseAt") Instant responseAt,
    //         @Param("responseMetadata") java.util.Map<String, Object> responseMetadata,
    //         @Param("status") String status
    // );
}
