package com.mailmind.rag;

import com.mailmind.ai.AiService;
import com.mailmind.dto.ApiDtos.*;
import com.mailmind.entity.*;
import com.mailmind.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) pipeline service.
 * Handles embedding storage, vector search, and context-aware chat.
 */
@Service
public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;
    private static final int TOP_K = 5;

    private final AiService aiService;
    private final JdbcTemplate jdbc;
    private final EmailRepository emailRepo;
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final GmailAccountRepository gmailAccountRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public RagService(AiService ai, JdbcTemplate jdbc, EmailRepository er,
                      ChatSessionRepository sr, ChatMessageRepository mr, GmailAccountRepository gar) {
        this.aiService = ai; this.jdbc = jdbc; this.emailRepo = er;
        this.sessionRepo = sr; this.messageRepo = mr; this.gmailAccountRepo = gar;
    }

    /** Create embeddings for an email's body text */
    public void embedEmail(Email email) {
        String text = email.getBodyText();
        if (text == null || text.isBlank()) return;
        // Delete existing embeddings for this email
        jdbc.update("DELETE FROM embeddings WHERE email_id = ?", email.getId());
        List<String> chunks = chunkText(text);
        for (int i = 0; i < chunks.size(); i++) {
            float[] emb = aiService.getEmbedding(chunks.get(i));
            String vecStr = "[" + floatsToString(emb) + "]";
            String metadata = String.format("{\"subject\":\"%s\",\"sender\":\"%s\",\"date\":\"%s\"}",
                escape(email.getSubject()), escape(email.getSenderEmail()),
                email.getReceivedAt() != null ? email.getReceivedAt().toString() : "");
            jdbc.update("INSERT INTO embeddings (id, gmail_account_id, email_id, thread_id, chunk_index, chunk_text, embedding, metadata) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?::vector, ?::jsonb)",
                email.getGmailAccount().getId(), email.getId(),
                email.getThread() != null ? email.getThread().getId() : null,
                i, chunks.get(i), vecStr, metadata);
        }
    }

    /** RAG chat query: embed question → vector search → LLM answer */
    public ChatQueryResponse chat(UUID userId, UUID gmailAccountId, String question, UUID sessionId) {
        // Get or create session
        ChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        } else {
            GmailAccount ga = gmailAccountRepo.findById(gmailAccountId).orElseThrow();
            session = sessionRepo.save(ChatSession.builder()
                .user(ga.getUser()).gmailAccount(ga).title(question.length() > 50 ? question.substring(0,50)+"..." : question)
                .isActive(true).build());
        }
        // Save user message
        messageRepo.save(ChatMessage.builder().session(session).role("user").content(question).build());
        // Get chat history for context
        List<ChatMessage> history = messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
        String chatHistory = history.size() > 1 ? history.subList(Math.max(0, history.size()-6), history.size()-1).stream()
            .map(m -> m.getRole() + ": " + m.getContent()).collect(Collectors.joining("\n")) : null;

        // 1. Get search/retrieval query plan
        String planJson = null;
        try {
            planJson = aiService.parseSearchPlan(question, chatHistory);
        } catch (Exception e) {
            log.error("Failed to parse search plan: {}", e.getMessage());
        }

        // Default plan parameters
        String sender = null;
        String subject = null;
        Integer timeRangeDays = null;
        String category = null;
        List<String> keywords = new ArrayList<>();
        boolean useVectorSearch = true;
        String vectorSearchQuery = question;

        if (planJson != null) {
            try {
                String cleanJson = planJson;
                if (planJson.contains("{")) {
                    cleanJson = planJson.substring(planJson.indexOf("{"), planJson.lastIndexOf("}") + 1);
                }
                JsonNode planNode = mapper.readTree(cleanJson);
                if (planNode.has("sender") && !planNode.get("sender").isNull()) {
                    sender = planNode.get("sender").asText().trim();
                    if (sender.isEmpty()) sender = null;
                }
                if (planNode.has("subject") && !planNode.get("subject").isNull()) {
                    subject = planNode.get("subject").asText().trim();
                    if (subject.isEmpty()) subject = null;
                }
                if (planNode.has("timeRangeDays") && !planNode.get("timeRangeDays").isNull()) {
                    timeRangeDays = planNode.get("timeRangeDays").asInt();
                }
                if (planNode.has("category") && !planNode.get("category").isNull()) {
                    category = planNode.get("category").asText().trim();
                    if (category.isEmpty()) category = null;
                }
                if (planNode.has("keywords") && planNode.get("keywords").isArray()) {
                    for (JsonNode kw : planNode.get("keywords")) {
                        String k = kw.asText().trim();
                        if (!k.isEmpty()) keywords.add(k);
                    }
                }
                if (planNode.has("useVectorSearch")) {
                    useVectorSearch = planNode.get("useVectorSearch").asBoolean();
                }
                if (planNode.has("vectorSearchQuery") && !planNode.get("vectorSearchQuery").isNull()) {
                    vectorSearchQuery = planNode.get("vectorSearchQuery").asText().trim();
                    if (vectorSearchQuery.isEmpty()) vectorSearchQuery = question;
                }
            } catch (Exception e) {
                log.warn("Error parsing search plan JSON: {}, response was: {}", e.getMessage(), planJson);
            }
        }

        // 2. Build dynamic SQL query for structured filter search
        StringBuilder sql = new StringBuilder("SELECT id, subject, sender_email, sender_name, received_at, snippet, ai_category, ai_summary, body_text, gmail_thread_id FROM emails WHERE gmail_account_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(gmailAccountId);

        if (sender != null) {
            sql.append(" AND (sender_email ILIKE ? OR sender_name ILIKE ?)");
            args.add("%" + sender + "%");
            args.add("%" + sender + "%");
        }
        if (subject != null) {
            sql.append(" AND subject ILIKE ?");
            args.add("%" + subject + "%");
        }
        if (category != null) {
            sql.append(" AND ai_category = ?");
            args.add(category);
        }
        if (timeRangeDays != null) {
            sql.append(" AND received_at >= CURRENT_TIMESTAMP - ? * INTERVAL '1 day'");
            args.add(timeRangeDays);
        }
        if (!keywords.isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("(body_text ILIKE ? OR subject ILIKE ?)");
                args.add("%" + keywords.get(i) + "%");
                args.add("%" + keywords.get(i) + "%");
            }
            sql.append(")");
        }

        sql.append(" ORDER BY received_at DESC LIMIT 20");

        List<Map<String, Object>> filteredEmails = jdbc.queryForList(sql.toString(), args.toArray());

        // 3. Execute vector search if requested, or as a fallback if no filtered emails found
        List<Map<String, Object>> vectorResults = new ArrayList<>();
        if (useVectorSearch || filteredEmails.isEmpty()) {
            float[] queryEmb = aiService.getEmbedding(vectorSearchQuery);
            String vecStr = "[" + floatsToString(queryEmb) + "]";
            vectorResults = jdbc.queryForList(
                "SELECT e.chunk_text, e.metadata, e.email_id, em.subject, em.sender_email, em.sender_name, em.received_at, em.gmail_thread_id "
                + "FROM embeddings e JOIN emails em ON e.email_id = em.id "
                + "WHERE e.gmail_account_id = ? ORDER BY e.embedding <=> ?::vector LIMIT ?",
                gmailAccountId, vecStr, TOP_K);
        }

        // 4. Combine context and sources
        StringBuilder context = new StringBuilder();
        List<SourceCitation> sources = new ArrayList<>();
        Set<UUID> seenEmails = new HashSet<>();

        // Process structured filter emails
        for (Map<String, Object> email : filteredEmails) {
            UUID emailId = (UUID) email.get("id");
            if (!seenEmails.contains(emailId)) {
                seenEmails.add(emailId);
                
                String emailSubj = (String) email.get("subject");
                String emailSender = (String) email.get("sender_email");
                String emailSenderName = (String) email.get("sender_name");
                Instant emailDate = email.get("received_at") != null 
                    ? (email.get("received_at") instanceof Timestamp 
                        ? ((Timestamp) email.get("received_at")).toInstant() 
                        : (Instant) email.get("received_at")) 
                    : null;
                String threadId = (String) email.get("gmail_thread_id");
                String snippet = (String) email.get("snippet");
                String body = (String) email.get("body_text");
                String summary = (String) email.get("ai_summary");
                
                context.append("### Email Source\n");
                context.append("Subject: ").append(emailSubj).append("\n");
                context.append("From: ").append(emailSenderName).append(" <").append(emailSender).append(">\n");
                context.append("Date: ").append(emailDate != null ? emailDate.toString() : "Unknown").append("\n");
                if (summary != null && !summary.isBlank()) {
                    context.append("AI Summary: ").append(summary).append("\n");
                }
                context.append("Content Snippet: ").append(snippet).append("\n");
                if (body != null && !body.isBlank()) {
                    context.append("Full Body (truncated): ").append(body.length() > 1500 ? body.substring(0, 1500) + "..." : body).append("\n");
                }
                context.append("---\n\n");

                sources.add(SourceCitation.builder().emailId(emailId)
                    .subject(emailSubj).senderEmail(emailSender).senderName(emailSenderName)
                    .date(emailDate).gmailThreadId(threadId).relevantSnippet(snippet).build());
            }
        }

        // Process vector search chunks
        for (Map<String, Object> r : vectorResults) {
            UUID emailId = (UUID) r.get("email_id");
            String chunkText = (String) r.get("chunk_text");
            String emailSubj = (String) r.get("subject");
            String emailSender = (String) r.get("sender_email");
            String emailSenderName = (String) r.get("sender_name");
            Instant emailDate = r.get("received_at") != null 
                ? (r.get("received_at") instanceof Timestamp 
                    ? ((Timestamp) r.get("received_at")).toInstant() 
                    : (Instant) r.get("received_at")) 
                : null;
            String threadId = (String) r.get("gmail_thread_id");

            context.append("### Relevant Chunk\n");
            context.append("Source Subject: ").append(emailSubj).append("\n");
            context.append("Source From: ").append(emailSenderName).append(" <").append(emailSender).append(">\n");
            context.append("Source Date: ").append(emailDate != null ? emailDate.toString() : "Unknown").append("\n");
            context.append("Chunk Text: ").append(chunkText).append("\n");
            context.append("---\n\n");

            if (!seenEmails.contains(emailId)) {
                seenEmails.add(emailId);
                sources.add(SourceCitation.builder().emailId(emailId)
                    .subject(emailSubj).senderEmail(emailSender).senderName(emailSenderName)
                    .date(emailDate).gmailThreadId(threadId).relevantSnippet(chunkText).build());
            }
        }

        // 5. Generate answer
        String answer = aiService.chatWithContext(question, context.toString(), chatHistory);
        // Save assistant message
        UUID[] sourceIds = seenEmails.toArray(new UUID[0]);
        messageRepo.save(ChatMessage.builder().session(session).role("assistant").content(answer).sourceEmailIds(sourceIds).build());

        return ChatQueryResponse.builder().sessionId(session.getId()).answer(answer).sources(sources).build();
    }

    // Text chunking with overlap
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text.length() <= CHUNK_SIZE) { chunks.add(text); return chunks; }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            start += CHUNK_SIZE - CHUNK_OVERLAP;
        }
        return chunks;
    }

    private String floatsToString(float[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) { if (i > 0) sb.append(","); sb.append(arr[i]); }
        return sb.toString();
    }

    private String escape(String s) { return s == null ? "" : s.replace("\"", "\\\"").replace("\n", " "); }
}
