package com.mailmind.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.*;

/**
 * Service for AI operations using Google Gemini (primary) and NVIDIA NIM (secondary).
 */
@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${gemini.api-key}") private String geminiKey;
    @Value("${gemini.model}") private String geminiModel;
    @Value("${gemini.embedding-model}") private String embeddingModel;
    @Value("${nvidia.nim.api-key}") private String nvidiaKey;
    @Value("${nvidia.nim.model}") private String nvidiaModel;

    private final WebClient geminiClient;
    private final WebClient nvidiaClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiService(@Qualifier("geminiWebClient") WebClient gc, @Qualifier("nvidiaWebClient") WebClient nc) {
        this.geminiClient = gc; this.nvidiaClient = nc;
    }

    /** Summarize a single email using Gemini */
    public String summarizeEmail(String bodyText, String subject, String sender) {
        String prompt = "Summarize this email clearly and concisely. Mention action items, deadlines, people, and important decisions if present.\n\n"
            + "Subject: " + subject + "\nFrom: " + sender + "\n\nEmail Body:\n" + truncate(bodyText, 8000);
        return callGemini(prompt);
    }

    /** Summarize a full thread */
    public String summarizeThread(List<Map<String, String>> messages) {
        StringBuilder ctx = new StringBuilder("Summarize this email thread. Include key decisions, action items, and the current status.\n\n");
        for (int i = 0; i < messages.size(); i++) {
            Map<String, String> m = messages.get(i);
            ctx.append("--- Message ").append(i+1).append(" ---\n");
            ctx.append("From: ").append(m.get("sender")).append("\nDate: ").append(m.get("date")).append("\n");
            ctx.append(truncate(m.get("body"), 3000)).append("\n\n");
        }
        return callGemini(ctx.toString());
    }

    private String normalizeCategory(String cat) {
        if (cat == null) return "Uncategorized";
        String normalized = cat.trim().toLowerCase();
        if (normalized.contains("work") || normalized.contains("professional")) return "Work/Professional";
        if (normalized.contains("personal")) return "Personal";
        if (normalized.contains("finance") || normalized.contains("money") || normalized.contains("billing") || normalized.contains("invoice")) return "Finance";
        if (normalized.contains("newsletter")) return "Newsletters";
        if (normalized.contains("job") || normalized.contains("recruit") || normalized.contains("career")) return "Job/Recruitment";
        if (normalized.contains("notification") || normalized.contains("alert") || normalized.contains("update")) return "Notifications";
        return "Uncategorized";
    }

    /** Categorize an email */
    public Map<String, Object> categorizeEmail(String bodyText, String subject, String sender) {
        String prompt = "Classify this email into exactly one category from: Newsletters, Job/Recruitment, Finance, Notifications, Personal, Work/Professional. Return JSON only: {\"category\": \"...\", \"confidence\": 0.0-1.0}\n\n"
            + "Subject: " + subject + "\nFrom: " + sender + "\nBody:\n" + truncate(bodyText, 4000);
        String resp = callGemini(prompt);
        try {
            // Extract JSON from response
            String json = resp;
            if (resp.contains("{")) json = resp.substring(resp.indexOf("{"), resp.lastIndexOf("}") + 1);
            JsonNode node = mapper.readTree(json);
            Map<String, Object> result = new HashMap<>();
            String cat = node.get("category").asText();
            result.put("category", normalizeCategory(cat));
            result.put("confidence", node.has("confidence") ? node.get("confidence").asDouble() : 0.8);
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse category response: {}", resp);
            return Map.of("category", "Uncategorized", "confidence", 0.0);
        }
    }

    /** Generate email draft from prompt */
    public String generateDraft(String prompt, String recipientInfo) {
        String fullPrompt = "Generate a professional email based on the following instruction. Write only the email body, no subject line.\n\n"
            + "Instruction: " + prompt + "\nRecipient: " + (recipientInfo != null ? recipientInfo : "Not specified");
        return callGemini(fullPrompt);
    }

    /** Generate thread-aware reply */
    public String generateReply(String instruction, List<Map<String, String>> threadContext) {
        StringBuilder ctx = new StringBuilder("Generate a professional reply based on the full email thread context and the user's instruction. Do not invent facts. Keep the tone polite and clear.\n\n");
        ctx.append("=== Thread Context ===\n");
        for (Map<String, String> m : threadContext) {
            ctx.append("From: ").append(m.get("sender")).append(" | Date: ").append(m.get("date")).append("\n");
            ctx.append(truncate(m.get("body"), 2000)).append("\n---\n");
        }
        ctx.append("\n=== User Instruction ===\n").append(instruction);
        return callGemini(ctx.toString());
    }

    /** RAG-based chat answer */
    public String chatWithContext(String question, String emailContext, String chatHistory) {
        String prompt = "You are an advanced Email Intelligence Assistant. Your role is to answer user queries using only their email data.\n\n"
            + "CRITICAL INSTRUCTIONS:\n"
            + "1. EXCLUSIVE KNOWLEDGE BASE: Base your answer *strictly* on the provided Email Context. Do not use external facts, assumptions, or guess. If the context does not contain the answer, say: \"I couldn't find any information about that in your emails.\"\n"
            + "2. SOURCE CLARITY: For *every* claim, summary point, or list item you write, you must cite the source email. Use a clear format at the end of the sentence or paragraph, e.g., \"[Source: 'Subject Line' from sender@example.com on YYYY-MM-DD]\". Do not write anything without attributing it to a specific source.\n"
            + "3. CROSS-EMAIL REASONING: Synthesize information from multiple emails and threads. Group similar topics together. If the query asks for newsletter/tech news or updates from multiple sources, present a clean, organized list, removing duplicate stories where the same event/story appears in different emails, and attribute the unified points to all their source emails.\n"
            + "4. CONVERSATIONAL CONTEXT: Use the 'Previous Chat' history to understand follow-up questions, pronouns (he/she/it/they), or context, but always answer based on the 'Email Context'.\n\n"
            + "=== Email Context ===\n" + emailContext + "\n\n"
            + (chatHistory != null ? "=== Previous Chat ===\n" + chatHistory + "\n\n" : "")
            + "=== Question ===\n" + question;
        return callGemini(prompt);
    }

    /** Generate a search/retrieval query plan from user query and chat history */
    public String parseSearchPlan(String question, String chatHistory) {
        String prompt = "Analyze the following user query about their emails and any recent chat context.\n"
            + "Your goal is to parse it into a JSON search plan that filters emails accurately in a SQL database.\n\n"
            + "Output ONLY a raw JSON block with the following fields (no markdown formatting, no other text):\n"
            + "{\n"
            + "  \"sender\": string or null (substring to match against sender_email or sender_name),\n"
            + "  \"subject\": string or null (substring to match against email subject),\n"
            + "  \"timeRangeDays\": integer or null (number of days from current time to look back),\n"
            + "  \"category\": string or null (one of: \"Newsletters\", \"Job/Recruitment\", \"Finance\", \"Notifications\", \"Personal\", \"Work/Professional\"),\n"
            + "  \"keywords\": array of strings (list of keywords/phrases to search in email body/subject, e.g. [\"reject\", \"denied\"]),\n"
            + "  \"useVectorSearch\": boolean (true if the query is semantic/open-ended, false if it is a specific metadata query),\n"
            + "  \"vectorSearchQuery\": string or null (semantic search query if useVectorSearch is true, else null)\n"
            + "}\n\n"
            + "User Query: " + question + "\n"
            + "Current Date/Time: " + java.time.Instant.now().toString() + "\n"
            + "Recent Chat Context:\n" + (chatHistory != null ? chatHistory : "None") + "\n\n"
            + "JSON:";
        return callGemini(prompt);
    }


    /** Get text embedding from Gemini */
    public float[] getEmbedding(String text) {
        try {
            String url = "/v1beta/models/" + embeddingModel + ":embedContent?key=" + geminiKey;
            Map<String, Object> body = Map.of("model", "models/" + embeddingModel,
                "content", Map.of("parts", List.of(Map.of("text", truncate(text, 5000)))));
            String resp = geminiClient.post().uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(body).retrieve().bodyToMono(String.class).block();
            JsonNode node = mapper.readTree(resp);
            JsonNode values = node.get("embedding").get("values");
            float[] embedding = new float[values.size()];
            for (int i = 0; i < values.size(); i++) embedding[i] = (float) values.get(i).asDouble();
            return embedding;
        } catch (Exception e) {
            log.error("Embedding failed: {}", e.getMessage());
            return new float[768]; // Return zero vector as fallback
        }
    }

    /** Call Gemini API */
    private String callGemini(String prompt) {
        try {
            String url = "/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiKey;
            Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 2048));
            String resp = geminiClient.post().uri(url).header("Content-Type", "application/json")
                .bodyValue(body).retrieve().bodyToMono(String.class).block();
            JsonNode node = mapper.readTree(resp);
            return node.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
        } catch (Exception e) {
            log.error("Gemini call failed, trying NVIDIA NIM: {}", e.getMessage());
            return callNvidia(prompt);
        }
    }

    /** Fallback to NVIDIA NIM */
    private String callNvidia(String prompt) {
        try {
            Map<String, Object> body = Map.of("model", nvidiaModel, "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3, "max_tokens", 2048);
            String resp = nvidiaClient.post().uri("/chat/completions")
                .header("Authorization", "Bearer " + nvidiaKey).header("Content-Type", "application/json")
                .bodyValue(body).retrieve().bodyToMono(String.class).block();
            JsonNode node = mapper.readTree(resp);
            return node.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            log.error("NVIDIA NIM also failed: {}", e.getMessage());
            return "AI service temporarily unavailable. Please try again later.";
        }
    }

    private String truncate(String text, int maxLen) {
        return text == null ? "" : (text.length() > maxLen ? text.substring(0, maxLen) + "..." : text);
    }
}
