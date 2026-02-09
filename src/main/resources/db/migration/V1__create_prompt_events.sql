CREATE TABLE prompt_events (
  id            UUID PRIMARY KEY,
  prompt        TEXT NOT NULL,
  captured_at   TIMESTAMPTZ NOT NULL,
  page_url      TEXT,
  user_agent    TEXT,
  device_id     VARCHAR(120),
  extension_version VARCHAR(40),
  send_method   VARCHAR(10),
  prompt_length INTEGER NOT NULL,
  prompt_hash   VARCHAR(64),
  metadata      JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Useful indexes
CREATE INDEX idx_prompt_events_captured_at ON prompt_events (captured_at DESC);
CREATE INDEX idx_prompt_events_device_id ON prompt_events (device_id);
CREATE INDEX idx_prompt_events_prompt_hash ON prompt_events (prompt_hash);

-- JSONB index (optional now, helpful later)
CREATE INDEX idx_prompt_events_metadata_gin ON prompt_events USING GIN (metadata);
