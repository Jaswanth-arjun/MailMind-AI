# MailMind AI — Architecture Document

## 1. System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Next.js 15)                     │
│   Landing │ Dashboard │ Inbox │ Chat │ Compose │ Categories      │
│                    ↕ REST API (JWT Auth)                         │
├─────────────────────────────────────────────────────────────────┤
│                     BACKEND (Spring Boot 3)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐    │
│  │  Gmail    │  │    AI    │  │   RAG    │  │   Security   │    │
│  │ Service   │  │ Service  │  │ Service  │  │ JWT + CORS   │    │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────────────┘    │
│       │              │              │                             │
│       ↓              ↓              ↓                             │
│  Gmail API     Gemini / NIM    pgvector Search                   │
├─────────────────────────────────────────────────────────────────┤
│                   DATABASE (Supabase PostgreSQL)                  │
│  users │ gmail_accounts │ emails │ threads │ embeddings │ chat   │
│                   + pgvector extension                           │
└─────────────────────────────────────────────────────────────────┘
```

### Request Flow
1. **User** → Next.js frontend (client-side React)
2. **Frontend** → Spring Boot REST API (authenticated via JWT Bearer token)
3. **Backend** → Gmail API (OAuth 2.0 access tokens), Gemini/NIM APIs
4. **Backend** → Supabase PostgreSQL (JPA + raw SQL for pgvector)
5. **Response** flows back through the same chain

---

## 2. Database Schema

### Entity Relationship

```
users
  └── gmail_accounts (1:N)
        ├── emails (1:N)
        │     └── embeddings (1:N, pgvector chunks)
        ├── threads (1:N)
        │     └── emails (1:N)
        ├── sync_state (1:1)
        ├── labels (1:N)
        ├── drafts (1:N)
        └── chat_sessions (via users, 1:N)
              └── chat_messages (1:N)
```

### Key Design Decisions
- **UUID primary keys**: No sequential IDs exposed externally
- **Gmail IDs stored separately**: `gmail_message_id` and `gmail_thread_id` for deduplication via unique constraints
- **Array columns**: PostgreSQL `text[]` for recipients, labels (denormalized for query performance)
- **JSONB metadata**: Embeddings store source metadata as JSONB for flexible attribution
- **pgvector index**: IVFFlat with cosine similarity for fast vector search
- **RLS policies**: Row-Level Security enabled for multi-tenant data isolation

---

## 3. AI Design

### Primary Model: Google Gemini 1.5 Flash
- **Why Gemini**: Fast inference, large context window (1M tokens), excellent instruction following, native embedding model (`text-embedding-004`), free tier generous for development
- **Used for**: Email summarization, thread summarization, categorization, reply generation, draft composition, RAG Q&A, and text embeddings

### Secondary Model: NVIDIA NIM (LLaMA 3.1 8B Instruct)
- **Why NIM**: Free tier available, OpenAI-compatible API, good fallback for when Gemini is rate-limited or unavailable
- **Used for**: Automatic fallback when Gemini API calls fail
- **Limitation**: No embedding model — Gemini is required for embeddings

### RAG Pipeline Design

```
User Question
      │
      ▼
┌─────────────┐     ┌──────────────┐     ┌───────────────┐
│  Embed      │────▶│  pgvector    │────▶│  Retrieve     │
│  Question   │     │  Similarity  │     │  Top-K Chunks │
│  (Gemini)   │     │  Search      │     │  + Metadata   │
└─────────────┘     └──────────────┘     └───────┬───────┘
                                                  │
                    ┌──────────────┐     ┌────────▼────────┐
                    │  Return      │◀────│  Gemini LLM     │
                    │  Answer +    │     │  (with context   │
                    │  Sources     │     │   + chat history)│
                    └──────────────┘     └─────────────────┘
```

### Prompt Engineering

| Task | Strategy |
|------|----------|
| **Chat Agent** | System prompt constrains to email context only; explicit instruction to cite sources; "not found" fallback |
| **Summarization** | Focus on action items, deadlines, people, decisions |
| **Categorization** | JSON-only output with confidence score; 6 fixed categories |
| **Reply Generation** | Full thread context provided; instruction-following; no hallucination |

### Hallucination Prevention
1. RAG grounds all chat answers in actual email content
2. System prompt explicitly forbids outside knowledge
3. Source citations are extracted from vector search metadata (not generated by LLM)
4. "Not found" response when no relevant emails match

### Chunking Strategy
- **Chunk size**: 1000 characters with 200 character overlap
- **Rationale**: Balances between capturing enough context per chunk and maintaining embedding quality
- **Embedding dimension**: 768 (Gemini `text-embedding-004`)

---

## 4. Gmail API Strategy

### OAuth 2.0 Flow
```
User → Frontend → Backend → Google Auth URL → User Grants Access
                                              → Google Callback
                                              → Exchange Code for Tokens
                                              → Store in gmail_accounts
                                              → Generate JWT → Frontend
```

### Initial Sync
1. Call `messages.list` with pagination (`maxResults=100`)
2. For each message ID, call `messages.get(format="full")`
3. Parse headers, extract body text (text/plain preference)
4. Create/update thread records
5. Save `pageToken` in `sync_state` for resume capability
6. After completion, store `historyId` from user profile

### Incremental Sync
1. Call `history.list(startHistoryId=lastHistoryId)`
2. Extract `messagesAdded` from history records
3. Fetch and save only new messages
4. Update `historyId`
5. If history expired (404), fall back to full sync

### Rate Limit Handling (429 Errors)
```
Exponential Backoff:
  Attempt 1: wait 1s
  Attempt 2: wait 2s
  Attempt 3: wait 4s
  Attempt 4: wait 8s
  Attempt 5: wait 16s
  + random jitter (10% of delay)
  Max retries: 5
```

### Sending Emails
- Build MIME message with proper headers
- Set `threadId` for replies to maintain Gmail thread context
- Set `In-Reply-To` and `References` headers for email clients

---

## 5. Tool & Technology Decisions

| Decision | Reasoning |
|----------|-----------|
| **Next.js 15 (App Router)** | Latest React with SSR/SSG capabilities, excellent DX, Vercel deployment |
| **Tailwind CSS 4** | Utility-first CSS with custom theme tokens, rapid UI development |
| **Spring Boot 3** | Production-grade Java backend, excellent Gmail API client libraries, JPA for database |
| **Supabase PostgreSQL** | Managed PostgreSQL with pgvector extension, free tier, easy setup |
| **pgvector** | Native PostgreSQL vector similarity search — no separate vector DB needed |
| **JWT authentication** | Stateless auth, works well with SPA frontends and API-first design |
| **WebFlux WebClient** | Non-blocking HTTP client for AI API calls (Gemini, NIM) |
| **Async sync** | `@Async` with dedicated thread pool for Gmail sync (doesn't block API) |

---

## 6. Trade-offs & Limitations

### Current Limitations

1. **No real-time sync**: Gmail sync is triggered manually (not via push notifications/watch). Adding `watch` would require a public webhook URL
2. **No attachment handling**: Email body text only; attachments are flagged but not downloaded or indexed
3. **Single Gmail account**: Current design supports one Gmail account per user
4. **Embedding cost**: Gemini embedding API has rate limits; bulk embedding of large mailboxes could hit limits
5. **No email search**: Full-text search on email content not yet implemented (FTS index would help)
6. **Token refresh**: OAuth token refresh is not yet implemented (would need scheduled background task)
7. **No WebSocket**: Chat responses are synchronous REST; WebSocket would enable streaming responses

### Design Trade-offs

| Trade-off | Decision | Rationale |
|-----------|----------|-----------|
| SQL vs NoSQL | PostgreSQL | Structured email data benefits from relational schema; pgvector eliminates need for separate vector DB |
| Gemini vs OpenAI | Gemini primary | Free tier is generous; native embedding model; good for technical assessment |
| Server-side vs client-side rendering | Client-side (CSR) | Dashboard is interactive; no SEO needed for authenticated pages |
| Monolith vs microservices | Monolith | Appropriate for MVP; faster to develop and deploy |
| REST vs GraphQL | REST | Simpler for this scope; well-suited for CRUD + action endpoints |
| Chunking size | 1000 chars | Balance between context quality and embedding precision |

### What I Would Add With More Time
1. Gmail push notifications via `watch()` API for real-time sync
2. OAuth token refresh flow
3. Email attachment processing and indexing
4. Full-text search with PostgreSQL tsvector
5. Streaming chat responses via WebSocket
6. Multi-account support
7. Newsletter deduplication using semantic similarity clustering
8. Batch processing pipeline for summarization/categorization
9. Email analytics and trends visualization
10. Export/sharing capabilities
