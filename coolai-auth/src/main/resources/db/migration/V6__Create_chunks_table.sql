-- V6: Create chunks table
CREATE TABLE chunks (
    id VARCHAR(36) PRIMARY KEY,
    doc_id VARCHAR(36) NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    vector_id VARCHAR(100),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chunks_doc_id ON chunks(doc_id);
CREATE INDEX idx_chunks_vector_id ON chunks(vector_id);
