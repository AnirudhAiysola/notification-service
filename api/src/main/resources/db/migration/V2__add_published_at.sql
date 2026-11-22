
ALTER TABLE notifications ADD COLUMN published_at TIMESTAMPTZ;
CREATE INDEX idx_notifications_unpublished ON notifications (created_at) WHERE published_at IS NULL;