# MailMind AI 🧠📧

> AI-powered Gmail Intelligence Platform — Technical Assessment for Repeatless AI Automation Executive

MailMind AI connects to your Gmail via OAuth 2.0, syncs emails, and provides AI-powered summarization, categorization, smart reply generation, and a RAG-based chat agent that answers questions using only your email data.

![Tech Stack](https://img.shields.io/badge/Frontend-Next.js_15-black?style=flat-square)
![Tech Stack](https://img.shields.io/badge/Backend-Spring_Boot_3-green?style=flat-square)
![Tech Stack](https://img.shields.io/badge/Database-Supabase_pgvector-blue?style=flat-square)
![Tech Stack](https://img.shields.io/badge/AI-Google_Gemini-orange?style=flat-square)
![Tech Stack](https://img.shields.io/badge/AI-NVIDIA_NIM-76B900?style=flat-square)

## 🌐 Live Deployment
- Frontend: `https://<your-frontend-domain>`
- Backend API: `https://<your-backend-domain>`
- GitHub: https://github.com/Jaswanth-arjun/MailMind-AI

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **Google OAuth Login** | Secure Gmail connection via OAuth 2.0 with token refresh |
| **Gmail Sync** | Initial + incremental sync using `historyId`, with exponential backoff for 429 errors |
| **AI Summarization** | Email and thread-level summaries via Gemini |
| **Smart Categorization** | Auto-classify into 6 categories: Newsletters, Job, Finance, Notifications, Personal, Work |
| **AI Chat Agent** | RAG pipeline: question → embedding → pgvector search → Gemini answer with source citations |
| **Thread-Aware Reply** | Generate replies with full conversation context |
| **AI Draft Composer** | Natural language prompt → professional email draft |

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Next.js 15, React 19, Tailwind CSS 4, TypeScript |
| Backend | Java 21, Spring Boot 3.3, Spring Security, WebFlux |
| Database | Supabase PostgreSQL + pgvector |
| Gmail | Google Gmail API (OAuth 2.0, not IMAP) |
| Primary AI | Google Gemini 1.5 Flash |
| Secondary AI | NVIDIA NIM (LLaMA 3.1 8B Instruct) — free tier |
| Deployment | Vercel (frontend), Render/Railway (backend), Supabase (database) |

---

## 📁 Project Structure

```
MailMind AI/
├── frontend/                    # Next.js 15 application
│   ├── src/
│   │   ├── app/                 # App router pages
│   │   │   ├── page.tsx         # Landing page
│   │   │   ├── dashboard/       # Dashboard layout + pages
│   │   │   │   ├── page.tsx     # Dashboard overview
│   │   │   │   ├── emails/      # Email list + detail
│   │   │   │   ├── chat/        # AI chat agent
│   │   │   │   ├── compose/     # Draft composer
│   │   │   │   ├── categories/  # Category filter
│   │   │   │   └── settings/    # Settings page
│   │   │   └── auth/callback/   # OAuth callback
│   │   ├── components/          # Reusable components
│   │   └── lib/                 # API service, types
│   └── package.json
├── backend/                     # Spring Boot 3 application
│   ├── src/main/java/com/mailmind/
│   │   ├── config/              # Security, WebClient, async config
│   │   ├── controller/          # REST API controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # JPA repositories
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── security/            # JWT auth
│   │   ├── gmail/               # Gmail API integration
│   │   ├── ai/                  # Gemini + NVIDIA NIM service
│   │   ├── rag/                 # RAG pipeline
│   │   └── exception/           # Error handling
│   └── pom.xml
├── database/
│   └── supabase-schema.sql      # Complete DB schema
├── .env.example                 # Environment template
├── .gitignore
├── README.md
└── Architecture.md
```

---

## 🚀 Setup Instructions

### Prerequisites

- **Java 21** (for backend)
- **Node.js 20+** (for frontend)
- **Supabase account** (free tier)
- **Google Cloud Console project** (for Gmail API)
- **Gemini API key** (Google AI Studio)
- **NVIDIA NIM API key** (optional, free tier)

### 1. Clone & Configure

```bash
git clone <repo-url>
cd "MailMind AI"
cp .env.example .env
# Edit .env with your credentials
```

### 2. Supabase Setup

1. Create a new Supabase project at [supabase.com](https://supabase.com)
2. Go to SQL Editor and run `database/supabase-schema.sql`
3. Copy your project URL, anon key, and database connection string to `.env`

### 3. Gmail OAuth Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a project → Enable Gmail API
3. Create OAuth 2.0 credentials (Web application)
4. Set redirect URI: `http://localhost:8080/api/gmail/callback`
5. Copy Client ID and Client Secret to `.env`

### 4. Gemini API Setup

1. Go to [Google AI Studio](https://aistudio.google.com/apikey)
2. Generate API key
3. Add to `.env` as `GEMINI_API_KEY`

### 5. NVIDIA NIM Setup (Optional)

1. Go to [NVIDIA NIM](https://build.nvidia.com)
2. Get free API key
3. Add to `.env` as `NVIDIA_NIM_API_KEY`

### 6. Run Backend

```bash
cd backend
./mvnw spring-boot:run
# API runs on http://localhost:8080
```

### 7. Run Frontend

```bash
cd frontend
npm install
npm run dev
# App runs on http://localhost:3000
```

---

## 🔑 Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_CLIENT_ID` | Yes | Google OAuth 2.0 Client ID |
| `GOOGLE_CLIENT_SECRET` | Yes | Google OAuth 2.0 Client Secret |
| `GOOGLE_REDIRECT_URI` | Yes | OAuth callback URL |
| `GEMINI_API_KEY` | Yes | Google Gemini API key |
| `NVIDIA_NIM_API_KEY` | No | NVIDIA NIM API key (fallback) |
| `SUPABASE_URL` | Yes | Supabase project URL |
| `SUPABASE_SERVICE_KEY` | Yes | Supabase service role key |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | Yes | DB username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | DB password |
| `APP_JWT_SECRET` | Yes | JWT signing secret (min 32 chars) |
| `APP_FRONTEND_URL` | No | Frontend URL (default: http://localhost:3000) |

---

## 🔌 REST API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/gmail/auth-url` | Get Google OAuth authorization URL |
| GET | `/api/gmail/callback` | OAuth callback handler |
| POST | `/api/gmail/sync` | Trigger email sync |
| GET | `/api/gmail/sync-status` | Get sync progress |
| GET | `/api/emails` | List emails (paginated, filterable) |
| GET | `/api/emails/{id}` | Get email detail |
| GET | `/api/threads/{threadId}` | Get thread with messages |
| GET | `/api/dashboard` | Get dashboard overview |
| POST | `/api/ai/summarize/email/{id}` | Summarize single email |
| POST | `/api/ai/summarize/thread/{id}` | Summarize thread |
| POST | `/api/ai/categorize/{id}` | Categorize email |
| POST | `/api/chat/query` | RAG chat query |
| POST | `/api/drafts/generate` | Generate AI draft |
| POST | `/api/drafts/send` | Send draft via Gmail |
| POST | `/api/reply/generate` | Generate thread-aware reply |
| POST | `/api/reply/send` | Send reply via Gmail |
| GET | `/api/health` | Health check |

---

## 🖼️ Screenshots

> Screenshots will be added after the application is deployed.

| Page | Description |
|------|-------------|
| Landing | Premium landing page with Google OAuth CTA |
| Dashboard | Email stats, category breakdown, recent emails |
| Inbox | Searchable, filterable email list with AI summaries |
| Email Detail | Full email view with AI summary and reply generation |
| AI Chat | RAG-powered chat with source-cited answers |
| Compose | AI draft generation with editable preview |
| Categories | Visual category breakdown with filtered views |
| Settings | Connection status, API config, model info |

---

## 🚢 Deployment

### Frontend → Vercel
```bash
cd frontend
npx vercel --prod
```
Set environment variable: `NEXT_PUBLIC_API_URL=https://your-backend.onrender.com`

### Backend → Render/Railway
- Deploy Spring Boot JAR
- Set all environment variables from `.env.example`
- Configure health check: `/api/health`

### Database → Supabase
- Already hosted on Supabase cloud
- Run `supabase-schema.sql` in SQL editor

---

## 📄 License

Built for the Repeatless AI Automation Executive technical assessment.
