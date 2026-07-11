CREATE TABLE notifications (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(30) NOT NULL CHECK (type IN ('TASK_ASSIGNED','COMMENT_CREATED','DUE_DATE_UPCOMING')),
    title         VARCHAR(150) NOT NULL,
    message       VARCHAR(500) NOT NULL,
    resource_type VARCHAR(30),
    resource_id   UUID,
    event_key     VARCHAR(200) UNIQUE,
    is_read       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_created
    ON notifications(user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, is_read)
    WHERE is_read = FALSE;
