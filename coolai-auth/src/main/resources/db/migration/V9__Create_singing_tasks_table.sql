-- V9: Create singing_tasks table
CREATE TABLE singing_tasks (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lyrics TEXT NOT NULL,
    voice_model_id VARCHAR(36) REFERENCES voice_models(id),
    style VARCHAR(50),
    engine VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    output_path VARCHAR(500),
    duration FLOAT,
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_singing_tasks_user_id ON singing_tasks(user_id);
CREATE INDEX idx_singing_tasks_status ON singing_tasks(status);
