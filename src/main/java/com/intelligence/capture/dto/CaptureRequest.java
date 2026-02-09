package com.intelligence.capture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CaptureRequest {

    @NotBlank
    @Size(max = 20000)
    public String prompt;

    @Size(max = 2000)
    public String pageUrl;

    // optional ISO-8601 time from browser
    public String capturedAt;

    @Size(max = 400)
    public String userAgent;

    @Size(max = 120)
    public String deviceId;

    @Size(max = 40)
    public String extensionVersion;

    @Size(max = 10)
    public String sendMethod; // enter|button
}
