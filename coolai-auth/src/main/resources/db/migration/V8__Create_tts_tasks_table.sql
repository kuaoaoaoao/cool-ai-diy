-- V8: Create tts_tasks table
CREATE TABLE tts_tasks (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    voice_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    output_path VARCHAR(500),
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_tts_tasks_user_id ON tts_tasks(user_id);
CREATE INDEX idx_tts_tasks_status ON tts_tasks(status);
