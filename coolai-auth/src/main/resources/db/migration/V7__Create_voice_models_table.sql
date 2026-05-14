-- V7: Create voice_models table
CREATE TABLE voice_models (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    model_path VARCHAR(500),
    ref_audio VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_voice_models_user_id ON voice_models(user_id);
CREATE INDEX idx_voice_models_status ON voice_models(status);
