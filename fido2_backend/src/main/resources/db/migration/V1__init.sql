-- Flyway V1 initial schema for FIDO2 backend on PostgreSQL

-- Users table: stores user identity and status flags
CREATE TABLE IF NOT EXISTS users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username           VARCHAR(150) UNIQUE NOT NULL,
    email              VARCHAR(254) UNIQUE NOT NULL,
    display_name       VARCHAR(255),
    is_active          BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Update trigger for updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_set_updated ON users;
CREATE TRIGGER trg_users_set_updated
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Authenticators: registered WebAuthn authenticators per user
CREATE TABLE IF NOT EXISTS authenticators (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id        BYTEA NOT NULL UNIQUE,               -- base64url-decoded
    public_key           BYTEA NOT NULL,                      -- COSE key (raw)
    sign_count           BIGINT NOT NULL DEFAULT 0,
    transports           VARCHAR(255),                        -- csv of transports
    aaguid               UUID,
    attestation_fmt      VARCHAR(100),
    registered_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at         TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_authenticators_user_id ON authenticators(user_id);
CREATE INDEX IF NOT EXISTS idx_authenticators_last_used_at ON authenticators(last_used_at);

-- Challenges: WebAuthn registration/authentication challenges
CREATE TABLE IF NOT EXISTS challenges (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID REFERENCES users(id) ON DELETE SET NULL,
    challenge_type       VARCHAR(32) NOT NULL,   -- 'registration' or 'authentication'
    challenge            BYTEA NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at           TIMESTAMPTZ NOT NULL,
    consumed_at          TIMESTAMPTZ,
    request_ip           INET,
    user_agent           TEXT
);

CREATE INDEX IF NOT EXISTS idx_challenges_user_id ON challenges(user_id);
CREATE INDEX IF NOT EXISTS idx_challenges_expires_at ON challenges(expires_at);
CREATE INDEX IF NOT EXISTS idx_challenges_consumed_at ON challenges(consumed_at);

-- QR-based login sessions
CREATE TABLE IF NOT EXISTS qr_sessions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_token        VARCHAR(128) UNIQUE NOT NULL, -- short-lived token shown as QR
    user_id              UUID REFERENCES users(id) ON DELETE SET NULL,
    status               VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, SCANNED, APPROVED, REJECTED, EXPIRED
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at           TIMESTAMPTZ NOT NULL,
    approved_at          TIMESTAMPTZ,
    rejected_at          TIMESTAMPTZ,
    scanned_at           TIMESTAMPTZ,
    client_ip            INET,
    device_info          TEXT
);

CREATE INDEX IF NOT EXISTS idx_qr_sessions_status ON qr_sessions(status);
CREATE INDEX IF NOT EXISTS idx_qr_sessions_expires_at ON qr_sessions(expires_at);

-- Audit logs for security-sensitive events
CREATE TABLE IF NOT EXISTS audit_logs (
    id                   BIGSERIAL PRIMARY KEY,
    occurred_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_id              UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type           VARCHAR(64) NOT NULL,  -- e.g., 'REGISTER', 'AUTHENTICATE', 'QR_APPROVED'
    source_ip            INET,
    user_agent           TEXT,
    details              JSONB DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_occurred_at ON audit_logs(occurred_at);

-- Ensure pgcrypto for gen_random_uuid(); on managed Postgres this might be pre-enabled.
-- Flyway will attempt to create extension if permitted; otherwise, ids should be provided by app layer.
DO $$
BEGIN
  PERFORM 1 FROM pg_extension WHERE extname = 'pgcrypto';
  IF NOT FOUND THEN
    CREATE EXTENSION IF NOT EXISTS pgcrypto;
  END IF;
END$$;

-- Optional: verify current timestamp drift-sensitive indexes by setting sensible default timezone
-- Not strictly necessary; included for clarity.
-- COMMENT ON DATABASE current_database() IS 'FIDO2 backend database';
