-- MailMind AI - Supabase PostgreSQL Schema
-- Full database schema with pgvector support for email intelligence platform

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- USERS TABLE
-- Stores authenticated user profiles
-- ============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ============================================================
-- GMAIL ACCOUNTS TABLE
-- Stores OAuth tokens and Gmail connection state per user
-- ============================================================
CREATE TABLE gmail_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    gmail_email VARCHAR(255) NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMPTZ,
    scopes TEXT,
    connected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_sync_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, gmail_email)
);

CREATE INDEX idx_gmail_accounts_user_id ON gmail_accounts(user_id);

-- ============================================================
-- SYNC STATE TABLE
-- Tracks Gmail sync progress and history ID for incremental sync
-- ============================================================
CREATE TABLE sync_state (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    last_history_id BIGINT,
    last_sync_started_at TIMESTAMPTZ,
    last_sync_completed_at TIMESTAMPTZ,
    sync_status VARCHAR(50) NOT NULL DEFAULT 'IDLE', -- IDLE, IN_PROGRESS, COMPLETED, FAILED
    total_messages_synced INTEGER DEFAULT 0,
    error_message TEXT,
    page_token TEXT, -- For resuming interrupted pagination
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(gmail_account_id)
);

CREATE INDEX idx_sync_state_gmail_account ON sync_state(gmail_account_id);

-- ============================================================
-- LABELS TABLE
-- Gmail labels (inbox, sent, categories, custom labels)
-- ============================================================
CREATE TABLE labels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    gmail_label_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50), -- system, user
    message_list_visibility VARCHAR(50),
    label_list_visibility VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(gmail_account_id, gmail_label_id)
);

CREATE INDEX idx_labels_gmail_account ON labels(gmail_account_id);

-- ============================================================
-- THREADS TABLE
-- Gmail thread aggregation
-- ============================================================
CREATE TABLE threads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    gmail_thread_id VARCHAR(255) NOT NULL,
    subject VARCHAR(1000),
    snippet TEXT,
    message_count INTEGER DEFAULT 0,
    last_message_at TIMESTAMPTZ,
    participants TEXT[], -- Array of email addresses in thread
    is_read BOOLEAN DEFAULT FALSE,
    ai_summary TEXT,
    ai_summary_generated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(gmail_account_id, gmail_thread_id)
);

CREATE INDEX idx_threads_gmail_account ON threads(gmail_account_id);
CREATE INDEX idx_threads_gmail_thread_id ON threads(gmail_thread_id);
CREATE INDEX idx_threads_last_message ON threads(last_message_at DESC);

-- ============================================================
-- EMAILS TABLE
-- Individual email messages
-- ============================================================
CREATE TABLE emails (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    thread_id UUID REFERENCES threads(id) ON DELETE CASCADE,
    gmail_message_id VARCHAR(255) NOT NULL,
    gmail_thread_id VARCHAR(255) NOT NULL,
    sender_email VARCHAR(255),
    sender_name VARCHAR(255),
    recipient_emails TEXT[],
    cc_emails TEXT[],
    bcc_emails TEXT[],
    subject VARCHAR(1000),
    snippet TEXT,
    body_text TEXT,
    body_html TEXT,
    received_at TIMESTAMPTZ,
    internal_date BIGINT,
    size_estimate INTEGER,
    is_read BOOLEAN DEFAULT FALSE,
    is_starred BOOLEAN DEFAULT FALSE,
    is_draft BOOLEAN DEFAULT FALSE,
    has_attachments BOOLEAN DEFAULT FALSE,
    gmail_label_ids TEXT[],
    -- AI-generated fields
    ai_summary TEXT,
    ai_summary_generated_at TIMESTAMPTZ,
    ai_category VARCHAR(100),
    ai_category_confidence FLOAT,
    ai_categorized_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(gmail_account_id, gmail_message_id)
);

CREATE INDEX idx_emails_gmail_account ON emails(gmail_account_id);
CREATE INDEX idx_emails_thread ON emails(thread_id);
CREATE INDEX idx_emails_gmail_message_id ON emails(gmail_message_id);
CREATE INDEX idx_emails_gmail_thread_id ON emails(gmail_thread_id);
CREATE INDEX idx_emails_sender ON emails(sender_email);
CREATE INDEX idx_emails_received_at ON emails(received_at DESC);
CREATE INDEX idx_emails_category ON emails(ai_category);

-- ============================================================
-- EMAIL LABELS JUNCTION TABLE
-- ============================================================
CREATE TABLE email_labels (
    email_id UUID NOT NULL REFERENCES emails(id) ON DELETE CASCADE,
    label_id UUID NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (email_id, label_id)
);

-- ============================================================
-- EMBEDDINGS TABLE
-- pgvector embeddings for RAG pipeline
-- ============================================================
CREATE TABLE embeddings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    email_id UUID REFERENCES emails(id) ON DELETE CASCADE,
    thread_id UUID REFERENCES threads(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL DEFAULT 0,
    chunk_text TEXT NOT NULL,
    embedding vector(768), -- Gemini embedding dimension
    metadata JSONB, -- Store subject, sender, date for source attribution
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Vector similarity search index (IVFFlat for performance)
CREATE INDEX idx_embeddings_vector ON embeddings
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX idx_embeddings_gmail_account ON embeddings(gmail_account_id);
CREATE INDEX idx_embeddings_email ON embeddings(email_id);
CREATE INDEX idx_embeddings_thread ON embeddings(thread_id);

-- ============================================================
-- CHAT SESSIONS TABLE
-- AI chat agent sessions
-- ============================================================
CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    title VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_active ON chat_sessions(is_active);

-- ============================================================
-- CHAT MESSAGES TABLE
-- Individual messages in chat sessions
-- ============================================================
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- 'user' or 'assistant'
    content TEXT NOT NULL,
    source_email_ids UUID[], -- References to emails used as sources
    source_metadata JSONB, -- Store source attribution details
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX idx_chat_messages_created ON chat_messages(created_at);

-- ============================================================
-- DRAFTS TABLE
-- AI-generated email drafts
-- ============================================================
CREATE TABLE drafts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    in_reply_to_email_id UUID REFERENCES emails(id),
    in_reply_to_thread_id UUID REFERENCES threads(id),
    recipient_emails TEXT[],
    cc_emails TEXT[],
    subject VARCHAR(1000),
    body_text TEXT,
    body_html TEXT,
    user_prompt TEXT, -- The instruction user gave
    ai_model_used VARCHAR(100),
    is_sent BOOLEAN NOT NULL DEFAULT FALSE,
    gmail_draft_id VARCHAR(255), -- If saved as Gmail draft
    gmail_message_id VARCHAR(255), -- If sent
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drafts_gmail_account ON drafts(gmail_account_id);
CREATE INDEX idx_drafts_reply_thread ON drafts(in_reply_to_thread_id);

-- ============================================================
-- NEWSLETTER ITEMS TABLE (Bonus feature)
-- Extracted news items for deduplication
-- ============================================================
CREATE TABLE newsletter_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email_id UUID NOT NULL REFERENCES emails(id) ON DELETE CASCADE,
    gmail_account_id UUID NOT NULL REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    title VARCHAR(1000),
    summary TEXT,
    source_name VARCHAR(255),
    source_url TEXT,
    embedding vector(768),
    dedup_cluster_id UUID, -- Groups similar stories
    is_duplicate BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_newsletter_items_email ON newsletter_items(email_id);
CREATE INDEX idx_newsletter_items_cluster ON newsletter_items(dedup_cluster_id);
CREATE INDEX idx_newsletter_items_vector ON newsletter_items
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 50);

-- ============================================================
-- UPDATED_AT TRIGGER FUNCTION
-- Auto-update updated_at timestamp
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to relevant tables
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_gmail_accounts_updated_at
    BEFORE UPDATE ON gmail_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sync_state_updated_at
    BEFORE UPDATE ON sync_state
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_threads_updated_at
    BEFORE UPDATE ON threads
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_emails_updated_at
    BEFORE UPDATE ON emails
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chat_sessions_updated_at
    BEFORE UPDATE ON chat_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_drafts_updated_at
    BEFORE UPDATE ON drafts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE gmail_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE emails ENABLE ROW LEVEL SECURITY;
ALTER TABLE threads ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE drafts ENABLE ROW LEVEL SECURITY;
ALTER TABLE embeddings ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- USEFUL VIEWS
-- ============================================================
CREATE OR REPLACE VIEW email_overview AS
SELECT
    e.id,
    e.gmail_message_id,
    e.sender_email,
    e.sender_name,
    e.subject,
    e.snippet,
    e.received_at,
    e.is_read,
    e.is_starred,
    e.ai_summary,
    e.ai_category,
    t.gmail_thread_id,
    t.message_count as thread_message_count,
    ga.gmail_email as account_email
FROM emails e
LEFT JOIN threads t ON e.thread_id = t.id
LEFT JOIN gmail_accounts ga ON e.gmail_account_id = ga.id
ORDER BY e.received_at DESC;
