-- V5: Create documents table
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    kb_id VARCHAR(36) NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT DEFAULT 0,
    file_type VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP
);

CREATE INDEX idx_documents_kb_id ON documents(kb_id);
CREATE INDEX idx_documents_status ON documents(status);
