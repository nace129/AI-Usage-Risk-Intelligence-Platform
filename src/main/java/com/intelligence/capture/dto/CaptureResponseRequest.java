package com.intelligence.capture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class CaptureResponseRequest {
  @NotNull public String turnId;
  @NotBlank @Size(max=60000) public String responseText; // cap to prevent abuse
  public String responseCapturedAt;
  public String modelHint; // optional if you can infer it
}

