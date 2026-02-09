Absolutely. Below is a **clean, professional README** tailored **exactly for your Phase 1 + Phase 2 commit**, written so that **if someone reads only this file, they understand what you built, why it matters, and how it maps to a real engineering role**.

You can drop this directly into `README.md` and commit it with your `v0.1.0` tag.

---

# AI Usage Risk Intelligence Platform

**Secure Cloud Backend Service (Spring Boot + Browser Extension)**

---

## Overview

This project implements a **production-style backend service and browser extension** that captures **user prompts and AI assistant responses** from ChatGPT in real time and stores them securely for analysis.

The system is designed to resemble a **real enterprise telemetry and compliance pipeline**, where AI usage must be monitored, audited, and evaluated for risk (e.g., PII leakage, policy violations, or sensitive data exposure).

Phase 1 and Phase 2 focus on **reliable data ingestion**, **secure backend architecture**, and **correct correlation of prompt–response turns** — the foundation required before advanced risk scoring or governance features can be built.

---

## Tech Stack

* **Language:** Java
* **Framework:** Spring Boot (REST APIs)
* **Browser Extension:** Chrome / Edge content script (Manifest V3)
* **Database:** PostgreSQL
* **Cloud Ready:** AWS (EC2, RDS, IAM)
* **Security Foundations:** Stateless APIs, hashed content fingerprints, environment-based secrets
* **Dev Tools:** Git, Postman, Maven

---

## Architecture

### High-Level Flow

```
Browser Extension (ChatGPT page)
        ↓
POST /api/turns/prompt
POST /api/turns/response
        ↓
Spring Boot Ingestion API
        ↓
Service Layer (validation, idempotency, hashing)
        ↓
PostgreSQL (prompt + response storage)
```

### Key Architectural Properties

* Stateless backend APIs
* Turn-based correlation using UUIDs
* Idempotent ingestion to prevent duplicate records
* Separation between capture logic (client) and policy logic (server)

---

## Key Features

### Phase 1 — Prompt Capture

* Captures user prompts from the ChatGPT UI at send time
* Generates a unique `turnId` per prompt
* Stores prompt text, timestamps, device metadata, and content hash
* Uses server-side validation and normalization

### Phase 2 — Response Capture & Correlation

* Captures AI assistant responses using a MutationObserver
* Correlates responses to prompts using the same `turnId`
* Handles asynchronous streaming responses
* Supports “n-1” response capture behavior (response finalized safely on next user action)
* Persists full prompt–response turns with status tracking (`PROMPT_ONLY`, `COMPLETED`)

### Backend Capabilities

* RESTful APIs for ingestion and verification
* SHA-256 hashing for content fingerprinting
* PostgreSQL persistence with indexed fields
* Debug and verification endpoint:

  * `GET /api/turns/recent?status=COMPLETED`

---

## Security Decisions

👉 **This section is intentional and differentiates the project**

* **Stateless ingestion design:** No server-side sessions; every request is self-contained.
* **Turn-based UUID correlation:** Prevents replay or accidental duplication of events.
* **No secrets in client code:** Extension sends only minimal metadata.
* **Hashed content fingerprints:** Enables deduplication and analytics without relying solely on raw text.
* **Environment-based configuration:** Database credentials and secrets are never hardcoded.
* **HTTPS-ready:** Designed to be deployed behind TLS-enabled load balancers.
* **Input validation:** DTO-level validation prevents malformed or abusive payloads.

These choices mirror real-world **AI governance, security, and compliance pipelines** used in enterprise environments.

---

## API Overview

### Capture Prompt

**POST** `/api/turns/prompt`

```json
{
  "turnId": "uuid-v4",
  "prompt": "hello",
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

### Verify Stored Data

**GET** `/api/turns/recent?status=COMPLETED`

Returns recently captured prompt–response pairs for verification and debugging.

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
   * Verify records via:

     ```bash
     curl http://localhost:8080/api/turns/recent?status=COMPLETED
     ```

---

## What This Project Demonstrates

If someone reads only this README, they should understand:

* **What was built:**
  A full prompt–response capture pipeline for AI usage.

* **Why it’s secure:**
  Stateless APIs, hashed data, environment-based secrets, and controlled ingestion.

* **How it maps to real jobs:**
  This mirrors systems used for AI governance, telemetry ingestion, security auditing, and compliance tooling in real companies.

---

## Future Improvements (Next Phases)

* Automated PII detection and redaction
* Risk scoring service for AI usage
* Asynchronous processing via Kafka or SQS
* Role-based dashboards for audits
* Rate limiting and abuse detection
* Enterprise auth (OAuth / SSO)

---

## Versioning

* **v0.1.0** — Phase 1 + Phase 2: reliable prompt–response capture and backend ingestion

