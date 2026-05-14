-- V3: Create messages table
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    conv_id VARCHAR(36) NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}',
    tokens INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conv_id ON messages(conv_id);
CREATE INDEX idx_messages_conv_id_created_at ON messages(conv_id, created_at);
