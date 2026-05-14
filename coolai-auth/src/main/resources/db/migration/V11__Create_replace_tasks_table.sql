-- V11: Create replace_tasks table
CREATE TABLE replace_tasks (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_video_path VARCHAR(500) NOT NULL,
    target_face_path VARCHAR(500) NOT NULL,
    mode VARCHAR(30) NOT NULL DEFAULT 'face_swap',
    engine VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    output_path VARCHAR(500),
    has_watermark BOOLEAN NOT NULL DEFAULT true,
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_replace_tasks_user_id ON replace_tasks(user_id);
CREATE INDEX idx_replace_tasks_status ON replace_tasks(status);
