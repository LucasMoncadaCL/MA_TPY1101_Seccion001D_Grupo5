CREATE TABLE IF NOT EXISTS token_revocation (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(128) NOT NULL UNIQUE,
    user_id INTEGER,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_token_revocation_expires_at ON token_revocation (expires_at);

ALTER TABLE "user"
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS blocked_until TIMESTAMPTZ NULL,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ NULL;

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    event VARCHAR(120) NOT NULL,
    actor_user_id INTEGER,
    target_user_id INTEGER,
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log (created_at DESC);

