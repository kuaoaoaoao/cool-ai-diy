-- V10: Create video_tasks table
CREATE TABLE video_tasks (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    mode VARCHAR(30) NOT NULL DEFAULT 'text2vid',
    engine VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    output_path VARCHAR(500),
    duration FLOAT,
    resolution VARCHAR(20),
    fps INTEGER DEFAULT 24,
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_video_tasks_user_id ON video_tasks(user_id);
CREATE INDEX idx_video_tasks_status ON video_tasks(status);
