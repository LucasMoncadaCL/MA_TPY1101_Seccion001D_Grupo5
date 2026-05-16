CREATE TABLE IF NOT EXISTS outbox_events (
    event_id uuid PRIMARY KEY,
    aggregate_type varchar(100) NOT NULL,
    aggregate_id uuid NULL,
    event_type varchar(150) NOT NULL,
    payload text NOT NULL,
    occurred_at timestamptz NOT NULL DEFAULT now(),
    processed_at timestamptz NULL,
    retry_count integer NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status_occurred_at
    ON outbox_events(status, occurred_at);
