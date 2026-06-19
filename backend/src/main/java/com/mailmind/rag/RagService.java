package com.mailmind.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailmind.ai.AiService;
import com.mailmind.dto.ApiDtos.*;
import com.mailmind.entity.*;
import com.mailmind.repository.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * RAG (Retrieval-Augmented Generation) pipeline service.
 * Handles embedding storage, hybrid retrieval, and context-aware chat.
 */
@Service
public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;
    private static final int VECTOR_TOP_K = 12;
    private static final int STRUCTURED_LIMIT = 30;
    private static final int MAX_CONTEXT_EMAILS = 12;
    private static final int MAX_CONTEXT_CHARS = 24000;

    private final AiService aiService;
    private final JdbcTemplate jdbc;
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final GmailAccountRepository gmailAccountRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public RagService(AiService ai, JdbcTemplate jdbc, EmailRepository er,
                      ChatSessionRepository sr, ChatMessageRepository mr, GmailAccountRepository gar) {
        this.aiService = ai;
        this.jdbc = jdbc;
        this.sessionRepo = sr;
        this.messageRepo = mr;
        this.gmailAccountRepo = gar;
    }

    /** Create embeddings for an email's body text. */
    public void embedEmail(Email email) {
        String text = email.getBodyText();
        if (text == null || text.isBlank()) return;

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

    /** Chat query: classify, retrieve hybrid email evidence, then answer. */
    public ChatQueryResponse chat(UUID userId, UUID gmailAccountId, String question, UUID sessionId) {
        ChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        } else {
            GmailAccount ga = gmailAccountRepo.findById(gmailAccountId).orElseThrow();
            session = sessionRepo.save(ChatSession.builder()
                .user(ga.getUser())
                .gmailAccount(ga)
                .title(question.length() > 50 ? question.substring(0, 50) + "..." : question)
                .isActive(true)
                .build());
        }

        messageRepo.save(ChatMessage.builder().session(session).role("user").content(question).build());
        List<ChatMessage> history = messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
        String chatHistory = history.size() > 1
            ? history.subList(Math.max(0, history.size() - 6), history.size() - 1).stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"))
            : null;

        SearchPlan plan = buildSearchPlan(question, chatHistory);

        if (plan.generalScope && !looksMailRelated(question)) {
            String answer = aiService.generalChat(question, chatHistory);
            messageRepo.save(ChatMessage.builder().session(session).role("assistant").content(answer).sourceEmailIds(new UUID[0]).build());
            return ChatQueryResponse.builder().sessionId(session.getId()).answer(answer).sources(List.of()).build();
        }

        List<Map<String, Object>> strictEmails = searchStructuredEmails(gmailAccountId, plan, false);
        List<Map<String, Object>> broadEmails = searchStructuredEmails(gmailAccountId, plan, true);
        List<Map<String, Object>> vectorResults = searchVectorChunks(gmailAccountId, plan, true);
        if (vectorResults.isEmpty()) {
            vectorResults = searchVectorChunks(gmailAccountId, plan, false);
        }

        StringBuilder context = new StringBuilder();
        List<SourceCitation> sources = new ArrayList<>();
        Set<UUID> seenEmails = new LinkedHashSet<>();

        appendEmailResults(context, sources, seenEmails, strictEmails);
        appendEmailResults(context, sources, seenEmails, broadEmails);
        appendVectorResults(context, sources, seenEmails, vectorResults);

        String answer = context.length() == 0 && plan.generalScope
            ? aiService.generalChat(question, chatHistory)
            : aiService.chatWithContext(question, context.toString(), chatHistory);

        UUID[] sourceIds = seenEmails.toArray(new UUID[0]);
        messageRepo.save(ChatMessage.builder().session(session).role("assistant").content(answer).sourceEmailIds(sourceIds).build());

        return ChatQueryResponse.builder().sessionId(session.getId()).answer(answer).sources(sources).build();
    }

    private SearchPlan buildSearchPlan(String question, String chatHistory) {
        SearchPlan plan = new SearchPlan();
        plan.vectorSearchQuery = question;
        plan.useVectorSearch = true;

        try {
            String planJson = aiService.parseSearchPlan(question, chatHistory);
            if (planJson != null && planJson.contains("{")) {
                String cleanJson = planJson.substring(planJson.indexOf("{"), planJson.lastIndexOf("}") + 1);
                JsonNode planNode = mapper.readTree(cleanJson);
                plan.generalScope = planNode.has("answerScope")
                    && "GENERAL".equalsIgnoreCase(planNode.get("answerScope").asText(""));
                plan.sender = textOrNull(planNode, "sender");
                plan.subject = textOrNull(planNode, "subject");
                plan.category = normalizeCategory(textOrNull(planNode, "category"));
                if (planNode.has("timeRangeDays") && !planNode.get("timeRangeDays").isNull()) {
                    int days = planNode.get("timeRangeDays").asInt();
                    if (days > 0 && days <= 3650) plan.timeRangeDays = days;
                }
                if (planNode.has("keywords") && planNode.get("keywords").isArray()) {
                    for (JsonNode kw : planNode.get("keywords")) {
                        String keyword = kw.asText("").trim();
                        if (!keyword.isEmpty()) plan.keywords.add(keyword);
                    }
                }
                if (planNode.has("useVectorSearch")) {
                    plan.useVectorSearch = planNode.get("useVectorSearch").asBoolean(true);
                }
                String vectorQuery = textOrNull(planNode, "vectorSearchQuery");
                if (vectorQuery != null) plan.vectorSearchQuery = vectorQuery;
            }
        } catch (Exception e) {
            log.warn("Failed to parse search plan, using fallback search: {}", e.getMessage());
        }

        if (plan.keywords.isEmpty()) {
            plan.keywords.addAll(fallbackKeywords(question));
        }
        return plan;
    }

    private List<Map<String, Object>> searchStructuredEmails(UUID gmailAccountId, SearchPlan plan, boolean broad) {
        StringBuilder sql = new StringBuilder("SELECT id, subject, sender_email, sender_name, received_at, snippet, ai_category, ai_summary, body_text, gmail_thread_id FROM emails WHERE gmail_account_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(gmailAccountId);

        if (!broad && plan.sender != null) {
            sql.append(" AND (sender_email ILIKE ? OR sender_name ILIKE ?)");
            args.add("%" + plan.sender + "%");
            args.add("%" + plan.sender + "%");
        }
        if (!broad && plan.subject != null) {
            sql.append(" AND subject ILIKE ?");
            args.add("%" + plan.subject + "%");
        }
        if (!broad && plan.category != null) {
            sql.append(" AND ai_category = ?");
            args.add(plan.category);
        }
        if (plan.timeRangeDays != null) {
            sql.append(" AND received_at >= CURRENT_TIMESTAMP - ? * INTERVAL '1 day'");
            args.add(plan.timeRangeDays);
        }
        if (!plan.keywords.isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < plan.keywords.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("(body_text ILIKE ? OR subject ILIKE ? OR snippet ILIKE ? OR ai_summary ILIKE ? OR sender_email ILIKE ? OR sender_name ILIKE ?)");
                String pattern = "%" + plan.keywords.get(i) + "%";
                for (int j = 0; j < 6; j++) args.add(pattern);
            }
            sql.append(")");
        }

        sql.append(" ORDER BY received_at DESC LIMIT ?");
        args.add(STRUCTURED_LIMIT);
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    private List<Map<String, Object>> searchVectorChunks(UUID gmailAccountId, SearchPlan plan, boolean constrained) {
        if (!plan.useVectorSearch && constrained) return List.of();

        float[] queryEmb = aiService.getEmbedding(plan.vectorSearchQuery);
        String vecStr = "[" + floatsToString(queryEmb) + "]";
        StringBuilder sql = new StringBuilder(
            "SELECT e.chunk_text, e.metadata, e.email_id, em.subject, em.sender_email, em.sender_name, em.received_at, em.gmail_thread_id "
            + "FROM embeddings e JOIN emails em ON e.email_id = em.id WHERE e.gmail_account_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(gmailAccountId);

        if (constrained) {
            if (plan.sender != null) {
                sql.append(" AND (em.sender_email ILIKE ? OR em.sender_name ILIKE ?)");
                args.add("%" + plan.sender + "%");
                args.add("%" + plan.sender + "%");
            }
            if (plan.subject != null) {
                sql.append(" AND em.subject ILIKE ?");
                args.add("%" + plan.subject + "%");
            }
            if (plan.category != null) {
                sql.append(" AND em.ai_category = ?");
                args.add(plan.category);
            }
            if (plan.timeRangeDays != null) {
                sql.append(" AND em.received_at >= CURRENT_TIMESTAMP - ? * INTERVAL '1 day'");
                args.add(plan.timeRangeDays);
            }
        }

        sql.append(" ORDER BY e.embedding <=> ?::vector LIMIT ?");
        args.add(vecStr);
        args.add(VECTOR_TOP_K);
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    private void appendEmailResults(StringBuilder context, List<SourceCitation> sources, Set<UUID> seenEmails, List<Map<String, Object>> emails) {
        for (Map<String, Object> email : emails) {
            if (seenEmails.size() >= MAX_CONTEXT_EMAILS || context.length() >= MAX_CONTEXT_CHARS) return;
            UUID emailId = (UUID) email.get("id");
            if (!seenEmails.add(emailId)) continue;

            String subject = (String) email.get("subject");
            String sender = (String) email.get("sender_email");
            String senderName = (String) email.get("sender_name");
            Instant date = instantFrom(email.get("received_at"));
            String threadId = (String) email.get("gmail_thread_id");
            String snippet = (String) email.get("snippet");
            String body = (String) email.get("body_text");
            String summary = (String) email.get("ai_summary");

            context.append("### Email Source\n");
            context.append("Subject: ").append(subject).append("\n");
            context.append("From: ").append(senderName).append(" <").append(sender).append(">\n");
            context.append("Date: ").append(date != null ? date.toString() : "Unknown").append("\n");
            if (summary != null && !summary.isBlank()) {
                context.append("AI Summary: ").append(summary).append("\n");
            }
            context.append("Content Snippet: ").append(snippet).append("\n");
            if (body != null && !body.isBlank()) {
                context.append("Body: ").append(truncate(body, 1800)).append("\n");
            }
            context.append("---\n\n");

            sources.add(SourceCitation.builder()
                .emailId(emailId)
                .subject(subject)
                .senderEmail(sender)
                .senderName(senderName)
                .date(date)
                .gmailThreadId(threadId)
                .relevantSnippet(snippet)
                .build());
        }
    }

    private void appendVectorResults(StringBuilder context, List<SourceCitation> sources, Set<UUID> seenEmails, List<Map<String, Object>> vectorResults) {
        Set<UUID> chunkedEmails = new HashSet<>();
        for (Map<String, Object> r : vectorResults) {
            if (context.length() >= MAX_CONTEXT_CHARS) return;
            UUID emailId = (UUID) r.get("email_id");
            if (chunkedEmails.contains(emailId) && seenEmails.size() >= MAX_CONTEXT_EMAILS) continue;
            chunkedEmails.add(emailId);

            String chunkText = (String) r.get("chunk_text");
            String subject = (String) r.get("subject");
            String sender = (String) r.get("sender_email");
            String senderName = (String) r.get("sender_name");
            Instant date = instantFrom(r.get("received_at"));
            String threadId = (String) r.get("gmail_thread_id");

            context.append("### Relevant Chunk\n");
            context.append("Source Subject: ").append(subject).append("\n");
            context.append("Source From: ").append(senderName).append(" <").append(sender).append(">\n");
            context.append("Source Date: ").append(date != null ? date.toString() : "Unknown").append("\n");
            context.append("Chunk Text: ").append(chunkText).append("\n");
            context.append("---\n\n");

            if (!seenEmails.contains(emailId) && seenEmails.size() < MAX_CONTEXT_EMAILS) {
                seenEmails.add(emailId);
                sources.add(SourceCitation.builder()
                    .emailId(emailId)
                    .subject(subject)
                    .senderEmail(sender)
                    .senderName(senderName)
                    .date(date)
                    .gmailThreadId(threadId)
                    .relevantSnippet(chunkText)
                    .build());
            }
        }
    }

    private String textOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) return null;
        String value = node.get(field).asText("").trim();
        return value.isEmpty() ? null : value;
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;
        List<String> allowed = List.of("Newsletters", "Job/Recruitment", "Finance", "Notifications", "Personal", "Work/Professional");
        for (String allowedCategory : allowed) {
            if (allowedCategory.equalsIgnoreCase(category)) return allowedCategory;
        }
        return null;
    }

    private boolean looksMailRelated(String question) {
        String text = question.toLowerCase();
        return text.matches(".*\\b(email|mail|inbox|sender|subject|thread|message|receipt|invoice|newsletter|gmail|sent|received|reply|attachment|deadline|meeting|interview|offer|recruiter|follow up|follow-up)\\b.*");
    }

    private List<String> fallbackKeywords(String question) {
        Set<String> stopWords = Set.of("what", "when", "where", "which", "whose", "about", "from", "with", "that", "this", "there", "their", "have", "has", "did", "does", "was", "were", "mail", "email", "emails", "please", "tell", "show", "give", "find", "latest", "recent");
        return Arrays.stream(question.toLowerCase().replaceAll("[^a-z0-9@._ -]", " ").split("\\s+"))
            .map(String::trim)
            .filter(s -> s.length() >= 3 && !stopWords.contains(s))
            .distinct()
            .limit(8)
            .collect(Collectors.toList());
    }

    private Instant instantFrom(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        return null;
    }

    private String truncate(String text, int maxLen) {
        return text == null ? "" : (text.length() > maxLen ? text.substring(0, maxLen) + "..." : text);
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }
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
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", " ");
    }

    private static class SearchPlan {
        String sender;
        String subject;
        Integer timeRangeDays;
        String category;
        List<String> keywords = new ArrayList<>();
        boolean useVectorSearch;
        String vectorSearchQuery;
        boolean generalScope;
    }
}
