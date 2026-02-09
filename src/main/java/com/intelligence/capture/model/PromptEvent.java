package com.intelligence.capture.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "prompt_events")
public class PromptEvent {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    // ---- Conversation / lifecycle ----

    @Column(name = "turn_id", columnDefinition = "uuid", nullable = false)
    private UUID turnId;

    @Column(name = "status", length = 30, nullable = false)
    private String status;
    // e.g. RECEIVED, SENT, COMPLETED, FAILED

    // ---- Prompt data ----

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(name = "prompt_length", nullable = false)
    private Integer promptLength;

    @Column(name = "prompt_hash", length = 64)
    private String promptHash;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    // ---- Context ----

    @Column(name = "page_url", columnDefinition = "text")
    private String pageUrl;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "device_id", length = 120)
    private String deviceId;

    @Column(name = "extension_version", length = 40)
    private String extensionVersion;

    @Column(name = "send_method", length = 10)
    private String sendMethod;

    // ---- Metadata ----

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    // ---- Response data ----

    @Column(name = "response_text", columnDefinition = "text")
    private String responseText;

    @Column(name = "response_captured_at")
    private Instant responseCapturedAt;

    @Column(name = "response_hash", length = 64)
    private String responseHash;

    // @JdbcTypeCode(SqlTypes.JSON)
    // @Column(name = "response_metadata", columnDefinition = "jsonb")
    // private Map<String, Object> responseMetadata;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> responseMetadata = new HashMap<>();

    // ---- Audit ----

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PromptEvent() {}

    @Column(name = "response_length")
    private Integer responseLength;

    public Integer getResponseLength() { return responseLength; }
    public void setResponseLength(Integer responseLength) { this.responseLength = responseLength; }


    // --- Getters / Setters (generate via IDE) ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTurnId() { return turnId; }
    public void setTurnId(UUID turnId) { this.turnId = turnId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public Integer getPromptLength() { return promptLength; }
    public void setPromptLength(Integer promptLength) { this.promptLength = promptLength; }

    public String getPromptHash() { return promptHash; }
    public void setPromptHash(String promptHash) { this.promptHash = promptHash; }

    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getExtensionVersion() { return extensionVersion; }
    public void setExtensionVersion(String extensionVersion) { this.extensionVersion = extensionVersion; }

    public String getSendMethod() { return sendMethod; }
    public void setSendMethod(String sendMethod) { this.sendMethod = sendMethod; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public Instant getResponseCapturedAt() { return responseCapturedAt; }
    public void setResponseCapturedAt(Instant responseCapturedAt) { this.responseCapturedAt = responseCapturedAt; }

    public String getResponseHash() { return responseHash; }
    public void setResponseHash(String responseHash) { this.responseHash = responseHash; }

    public Map<String, Object> getResponseMetadata() { return responseMetadata; }
    public void setResponseMetadata(Map<String, Object> responseMetadata) {
        this.responseMetadata = responseMetadata;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

// package com.intelligence.capture.model;

// import org.hibernate.annotations.JdbcTypeCode;
// import org.hibernate.type.SqlTypes;
// import jakarta.persistence.*;

// import java.time.Instant;
// import java.util.Map;
// import java.util.UUID;

// @Entity
// @Table(name = "prompt_events")
// public class PromptEvent {

//     @Id
//     @Column(columnDefinition = "uuid")
//     private UUID id;

//     @Column(nullable = false, columnDefinition = "text")
//     private String prompt;

//     @Column(name = "captured_at", nullable = false)
//     private Instant capturedAt;

//     @Column(name = "page_url", columnDefinition = "text")
//     private String pageUrl;

//     @Column(name = "user_agent", columnDefinition = "text")
//     private String userAgent;

//     @Column(name = "device_id", length = 120)
//     private String deviceId;

//     @Column(name = "extension_version", length = 40)
//     private String extensionVersion;

//     @Column(name = "send_method", length = 10)
//     private String sendMethod;

//     @Column(name = "prompt_length", nullable = false)
//     private Integer promptLength;

//     @Column(name = "prompt_hash", length = 64)
//     private String promptHash;

//     @JdbcTypeCode(SqlTypes.JSON)
//     @Column(nullable = false, columnDefinition = "jsonb")
//     private Map<String, Object> metadata;    

//     @Column(name = "created_at", nullable = false)
//     private Instant createdAt;

//     public PromptEvent() {}

//     // --- Getters/Setters (generate with IDE) ---
//     public UUID getId() { return id; }
//     public void setId(UUID id) { this.id = id; }

//     public String getPrompt() { return prompt; }
//     public void setPrompt(String prompt) { this.prompt = prompt; }

//     public Instant getCapturedAt() { return capturedAt; }
//     public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }

//     public String getPageUrl() { return pageUrl; }
//     public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

//     public String getUserAgent() { return userAgent; }
//     public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

//     public String getDeviceId() { return deviceId; }
//     public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

//     public String getExtensionVersion() { return extensionVersion; }
//     public void setExtensionVersion(String extensionVersion) { this.extensionVersion = extensionVersion; }

//     public String getSendMethod() { return sendMethod; }
//     public void setSendMethod(String sendMethod) { this.sendMethod = sendMethod; }

//     public Integer getPromptLength() { return promptLength; }
//     public void setPromptLength(Integer promptLength) { this.promptLength = promptLength; }

//     public String getPromptHash() { return promptHash; }
//     public void setPromptHash(String promptHash) { this.promptHash = promptHash; }

//     public Map<String, Object> getMetadata() { return metadata; }
//     public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

//     public Instant getCreatedAt() { return createdAt; }
//     public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
// }
