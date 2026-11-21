-- =========================================
-- 1. notifications: every accepted request
-- =========================================
CREATE TABLE notifications (
                               id              UUID PRIMARY KEY,
                               idempotency_key VARCHAR(200) NOT NULL UNIQUE,
                               user_id         BIGINT       NOT NULL,
                               template_id     VARCHAR(100) NOT NULL,
                               priority        VARCHAR(10)  NOT NULL CHECK (priority IN ('HIGH', 'LOW')),
                               category        VARCHAR(20)  NOT NULL CHECK (category IN ('TRANSACTIONAL', 'MARKETING')),
                               variables       JSONB,
                               expires_at      TIMESTAMPTZ  NOT NULL,
                               created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE deliveries (
                            id              BIGSERIAL PRIMARY KEY,
                            notification_id UUID        NOT NULL REFERENCES notifications(id),
                            channel         VARCHAR(10) NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
                            status          VARCHAR(10) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'SKIPPED', 'EXPIRED')),
                            attempts        INT         NOT NULL DEFAULT 0,
                            last_error      TEXT,
                            updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                            UNIQUE (notification_id, channel)
);

-- =========================================
-- 3–5. Lookup tables (given)
-- =========================================
CREATE TABLE templates (
                           id       VARCHAR(100) NOT NULL,
                           channel  VARCHAR(10)  NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
                           subject  VARCHAR(200),             -- only email uses a subject
                           body     TEXT         NOT NULL,
                           PRIMARY KEY (id, channel)          -- same template name, one version per channel
);

CREATE TABLE user_contacts (
                               user_id    BIGINT PRIMARY KEY,
                               email      VARCHAR(200),
                               phone      VARCHAR(30),
                               push_token VARCHAR(200)
);

CREATE TABLE user_preferences (
                                  user_id  BIGINT      NOT NULL,
                                  channel  VARCHAR(10) NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
                                  category VARCHAR(20) NOT NULL CHECK (category IN ('TRANSACTIONAL', 'MARKETING')),
                                  enabled  BOOLEAN     NOT NULL,
                                  PRIMARY KEY (user_id, channel, category)
);