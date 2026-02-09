ALTER TABLE prompt_events
  ADD COLUMN turn_id UUID UNIQUE,
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PROMPT_ONLY',
  ADD COLUMN response_text TEXT,
  ADD COLUMN response_captured_at TIMESTAMPTZ,
  ADD COLUMN response_length INTEGER,
  ADD COLUMN response_hash VARCHAR(64),
  ADD COLUMN response_metadata JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX idx_prompt_events_turn_id ON prompt_events (turn_id);
CREATE INDEX idx_prompt_events_status ON prompt_events (status);
