Below is a **fully updated README** that incorporates **Phase 3 (PII detection + risk scoring)** while preserving the clean, job-aligned structure you asked for earlier.
You can **copy–paste this directly into `README.md`** in
`https://github.com/nace129/AI-Usage-Risk-Intelligence-Platform`.

---

# AI Usage Risk Intelligence Platform

**Secure Cloud Backend Service (Spring Boot + Browser Extension)**

---

## Overview

AI Usage Risk Intelligence Platform is a **production-style backend system with a browser extension** that captures **user prompts and AI assistant responses** from ChatGPT and evaluates them for **privacy and security risk**.

The system is designed to mirror how companies monitor **LLM usage, sensitive data exposure, and compliance risk** in enterprise environments.

* **Phase 1**: Reliable prompt ingestion
* **Phase 2**: Robust prompt–response correlation
* **Phase 3**: Automated **PII detection + risk scoring pipeline**

This project intentionally focuses on **security, auditability, and real-world architecture decisions**, not just data collection.

---

## Tech Stack

* **Language:** Java
* **Framework:** Spring Boot (REST APIs)
* **Browser Extension:** Chrome / Edge (Manifest V3, content script)
* **Database:** PostgreSQL
* **Cloud Ready:** AWS (EC2, RDS, IAM)
* **Security Foundations:** Stateless APIs, hashing, environment-based secrets
* **Dev Tools:** Git, Maven, Postman

---

## Architecture

### High-Level Flow

<img width="1408" height="768" alt="generated-image-228bf2a4-571a-4251-b840-1302cc5241cd" src="https://github.com/user-attachments/assets/a49ace56-a63a-4df4-976e-a290bbcbbe68" />


### Architectural Principles

* Stateless backend APIs
* UUID-based turn correlation
* Asynchronous risk analysis
* Separation of ingestion, scoring, and reporting
* Designed for cloud deployment behind HTTPS load balancers

---

## Key Features

### Phase 1 — Prompt Capture

* Captures user prompts at send time from ChatGPT UI
* Generates a unique `turnId` per prompt
* Stores prompt text, timestamps, device metadata, and SHA-256 hash
* Input validation and normalization

### Phase 2 — Response Capture & Correlation

* Captures AI assistant responses using `MutationObserver`
* Correlates responses to prompts via `turnId`
* Handles streaming responses and UI timing edge cases
* Stores complete prompt–response “turns”
* Status tracking: `PROMPT_ONLY`, `COMPLETED`

### Phase 3 — Risk Scoring & PII Detection

* Automatically analyzes **prompt + response text**
* Detects common PII using regex-based detectors:

  * Email
  * SSN
  * Phone number
  * Credit card–like patterns
* Computes an **explainable risk score (0.0 – 1.0)**
* Stores structured results in `risk_scores`
* Updates turn status:

  * `CLEARED`
  * `REVIEW`
  * `FLAGGED`
* Redacts sensitive content when risk is high
* Risk scoring runs **asynchronously** so ingestion remains fast

---

## Security Decisions

👉 **This section is intentional and differentiates the project**

* **Stateless ingestion:** No server-side sessions; every request is self-contained.
* **Turn-based UUID correlation:** Prevents duplicate or mismatched events.
* **No secrets in client code:** Extension sends only minimal metadata.
* **Hashed content fingerprints:** Enables deduplication and analytics without relying solely on raw text.
* **Environment-based configuration:** Database credentials and secrets are never hardcoded.
* **HTTPS-ready:** Designed to be deployed behind TLS-enabled load balancers.
* **PII-aware pipeline:** Sensitive data is detected, scored, and optionally redacted before long-term storage.
* **Explainable scoring:** Risk decisions are rule-based and auditable (no black-box ML).

These choices mirror real-world **AI governance, security, and compliance systems** used in enterprise environments.

---

## API Overview

### Capture Prompt

**POST** `/api/turns/prompt`

```json
{
  "turnId": "uuid-v4",
  "prompt": "my email is abc@gmail.com",
  "pageUrl": "https://chatgpt.com/",
  "capturedAt": "2026-02-09T05:11:13.889Z",
  "userAgent": "Chrome/...",
  "deviceId": "device-uuid",
  "extensionVersion": "1.0.0",
  "sendMethod": "enter"
}
```

---

### Capture Response

**POST** `/api/turns/response`

```json
{
  "turnId": "uuid-v4",
  "responseText": "Assistant response...",
  "responseCapturedAt": "2026-02-09T05:11:19.920Z"
}
```

---

### Verify Stored Turns

**GET** `/api/turns/recent?status=COMPLETED`

---

### View Risk Score for a Turn

**GET** `/api/turns/{turnId}/risk`

Returns the latest risk score and detected PII details.

---

### View Recent Risky Turns

**GET** `/api/turns/risk/recent?minScore=0.4`

Returns recently flagged or high-risk interactions.

---

## How to Run (Local Development)

1. **Clone the repository**

   ```bash
   git clone https://github.com/nace129/AI-Usage-Risk-Intelligence-Platform.git
   cd AI-Usage-Risk-Intelligence-Platform
   ```

2. **Configure environment variables**

   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gpt_capture
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=secret
   ```

3. **Start PostgreSQL**

   ```bash
   docker run -e POSTGRES_PASSWORD=secret -p 5432:5432 -d postgres:15
   ```

4. **Run the backend**

   ```bash
   ./mvnw spring-boot:run
   ```

5. **Load the browser extension**

   * Open `chrome://extensions`
   * Enable **Developer mode**
   * Click **Load unpacked**
   * Select the extension folder containing `manifest.json` and `content.js`

6. **Test the system**

   * Send prompts in ChatGPT
   * Verify ingestion:

     ```bash
     curl http://localhost:8080/api/turns/recent
     ```
   * Verify risk scoring:

     ```bash
     curl http://localhost:8080/api/turns/risk/recent?minScore=0.1
     ```

---

## What This Project Demonstrates

If someone reads only this README, they should understand:

* **What was built:**
  A full ingestion and analysis pipeline for AI usage.

* **Why it’s secure:**
  Stateless APIs, hashed data, environment-based secrets, and automated PII detection.

* **How it maps to real jobs:**
  This mirrors systems used for AI governance, telemetry ingestion, compliance tooling, and security analytics in real companies.

---

## Future Improvements

* Machine-learning–based PII classification
* Streaming ingestion via Kafka or SQS
* Configurable data retention policies
* Role-based dashboards for audits
* Rate limiting and abuse detection
* Enterprise authentication (OAuth / SSO)

---

## Versioning

* **v0.1.0** — Phase 1 + Phase 2: prompt–response capture
* **v0.2.0** — Phase 3: PII detection and risk scoring
