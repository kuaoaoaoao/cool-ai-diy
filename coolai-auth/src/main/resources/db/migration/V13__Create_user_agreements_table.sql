-- V13: Create user_agreements table
CREATE TABLE user_agreements (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    agreement_type VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    signed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_agreements_user_id ON user_agreements(user_id);
CREATE INDEX idx_user_agreements_type ON user_agreements(agreement_type);
