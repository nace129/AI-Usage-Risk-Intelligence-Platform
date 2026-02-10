CREATE TABLE IF NOT EXISTS risk_scores (
  id UUID PRIMARY KEY,
  turn_id UUID NOT NULL,
  scored_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  score DOUBLE PRECISION NOT NULL,
  details JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_risk_scores_turn_id ON risk_scores(turn_id);
CREATE INDEX IF NOT EXISTS idx_risk_scores_score ON risk_scores(score DESC);
CREATE INDEX IF NOT EXISTS idx_risk_scores_scored_at ON risk_scores(scored_at DESC);
