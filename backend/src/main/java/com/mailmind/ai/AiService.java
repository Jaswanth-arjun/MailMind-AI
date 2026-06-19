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

    private final String openaiKey = System.getenv("OPENAI_API_KEY");
    private final String openaiBaseUrl = System.getenv("API_BASE_URL") != null ? System.getenv("API_BASE_URL") : "https://api.openai.com/v1";
    private final String openaiModel = System.getenv("MODEL_NAME") != null ? System.getenv("MODEL_NAME") : "gpt-4o-mini";

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
            + "1. EXCLUSIVE KNOWLEDGE BASE: Answer the user's question using the provided Email Context. You are encouraged to count, summarize, and analyze the emails present in the context to answer count, summary, or details queries. Do not assume or invent facts outside the context. If the context does not contain any relevant emails or information to answer the question, say: \"I couldn't find any information about that in your emails.\"\n"
            + "2. SOURCE CLARITY: For *every* claim, summary point, or list item you write, you must cite the source email. Use a clear format at the end of the sentence or paragraph, e.g., \"[Source: 'Subject Line' from sender@example.com on YYYY-MM-DD]\". Do not write anything without attributing it to a specific source.\n"
            + "3. CROSS-EMAIL REASONING: Synthesize information from multiple emails and threads. Group similar topics together. If the query asks for newsletter/tech news or updates from multiple sources, present a clean, organized list, removing duplicate stories where the same event/story appears in different emails, and attribute the unified points to all their source emails.\n"
            + "4. CONVERSATIONAL CONTEXT: Use the 'Previous Chat' history to understand follow-up questions, pronouns (he/she/it/they), or context, but always answer based on the 'Email Context'.\n\n"
            + "=== Email Context ===\n" + emailContext + "\n\n"
            + (chatHistory != null ? "=== Previous Chat ===\n" + chatHistory + "\n\n" : "")
            + "=== Question ===\n" + question;
        return callGemini(prompt);
    }

    /** General chat answer for questions that are not asking about the user's mailbox. */
    public String generalChat(String question, String chatHistory) {
        String prompt = "You are MailMind's helpful AI assistant. Answer the user's general question accurately and clearly.\n"
            + "Do not claim that the answer came from the user's emails unless email context was provided. If the question needs live, private, legal, medical, or financial data you do not have, say what is missing and give safe general guidance.\n\n"
            + (chatHistory != null ? "=== Previous Chat ===\n" + chatHistory + "\n\n" : "")
            + "=== Question ===\n" + question;
        return callGemini(prompt);
    }

    /** Generate a search/retrieval query plan from user query and chat history */
    public String parseSearchPlan(String question, String chatHistory) {
        String prompt = "Analyze the following user query about their emails and any recent chat context.\n"
            + "Your goal is to parse it into a JSON search plan that filters emails accurately in a SQL database.\n"
            + "First decide answerScope: use \"MAIL\" when the user is asking about their inbox, messages, senders, threads, appointments, deadlines, receipts, newsletters, or follow-up context from prior email answers. Use \"GENERAL\" only when the user is clearly asking a normal non-email knowledge question.\n\n"
            + "Output ONLY a raw JSON block with the following fields (no markdown formatting, no other text):\n"
            + "{\n"
            + "  \"answerScope\": \"MAIL\" or \"GENERAL\",\n"
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


    /** Get text embedding from Gemini or OpenAI */
    public float[] getEmbedding(String text) {
        // 1. Try Gemini Embedding
        if (geminiKey != null && !geminiKey.trim().isEmpty()) {
            try {
                String url = "/v1beta/models/" + embeddingModel + ":embedContent";
                Map<String, Object> part = Map.of("text", truncate(text, 5000));
                Map<String, Object> content = Map.of("parts", List.of(part));
                Map<String, Object> body = Map.of(
                    "content", content,
                    "outputDimensionality", 768
                );
                String resp = geminiClient.post().uri(url)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiKey)
                    .bodyValue(body).retrieve().bodyToMono(String.class).block();
                JsonNode node = mapper.readTree(resp);
                float[] embedding = parseEmbedding(node);
                if (embedding != null) return embedding;
                log.error("Embedding response missing expected structure: {}", resp);
            } catch (Exception e) {
                log.error("Embedding failed: {}", e.getMessage());
            }
        }

        // 2. Try OpenAI Embedding
        float[] openAiEmbedding = getOpenAiEmbedding(text);
        if (openAiEmbedding != null) {
            return openAiEmbedding;
        }

        return new float[768]; // Return zero vector as fallback
    }

    private float[] parseEmbedding(JsonNode node) {
        if (node.has("predictions") && node.get("predictions").isArray()) {
            JsonNode pred = node.get("predictions").get(0);
            JsonNode embedding = pred.get("embedding");
            if (embedding != null && embedding.isArray()) {
                return jsonArrayToFloatArray(embedding);
            }
            JsonNode output = pred.get("output");
            if (output != null && output.has("embedding")) {
                return jsonArrayToFloatArray(output.get("embedding"));
            }
        }
        if (node.has("embedding") && node.get("embedding").has("values")) {
            return jsonArrayToFloatArray(node.get("embedding").get("values"));
        }
        if (node.has("predictions") && node.get("predictions").isArray()) {
            JsonNode first = node.get("predictions").get(0);
            if (first.has("data") && first.get("data").isArray()) {
                JsonNode dataEmb = first.get("data").get(0).get("embedding");
                if (dataEmb != null) return jsonArrayToFloatArray(dataEmb);
            }
        }
        return null;
    }

    private float[] jsonArrayToFloatArray(JsonNode array) {
        float[] embedding = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            embedding[i] = (float) array.get(i).asDouble();
        }
        return embedding;
    }

    /** Call Gemini API, fallback to OpenAI, NVIDIA NIM, and finally Local Mock Simulation */
    private String callGemini(String prompt) {
        // 1. Try Gemini
        if (geminiKey != null && !geminiKey.trim().isEmpty()) {
            try {
                String url = "/v1beta/models/" + geminiModel + ":generateContent";
                Map<String, Object> part = Map.of("text", prompt);
                Map<String, Object> content = Map.of("parts", List.of(part));
                Map<String, Object> genConfig = Map.of(
                    "temperature", 0.3,
                    "maxOutputTokens", 2048
                );
                Map<String, Object> body = Map.of(
                    "contents", List.of(content),
                    "generationConfig", genConfig
                );
                String resp = geminiClient.post().uri(url)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiKey)
                    .bodyValue(body).retrieve().bodyToMono(String.class).block();
                JsonNode node = mapper.readTree(resp);
                String result = parseTextResponse(node);
                if (result != null) return result;
                log.error("Gemini response missing expected structure: {}", resp);
            } catch (Exception e) {
                log.error("Gemini call failed, trying fallbacks: {}", e.getMessage());
            }
        }

        // 2. Try OpenAI
        String openAiResult = callOpenAi(prompt);
        if (openAiResult != null) {
            return openAiResult;
        }

        // 3. Try NVIDIA NIM
        if (nvidiaKey != null && !nvidiaKey.trim().isEmpty()) {
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
            }
        }

        // 4. Try Local Mock Simulator Fallback
        return getLocalMockResponse(prompt);
    }

    private String parseTextResponse(JsonNode node) {
        if (node.has("predictions") && node.get("predictions").isArray()) {
            JsonNode first = node.get("predictions").get(0);
            if (first.has("candidates") && first.get("candidates").isArray()) {
                JsonNode candidate = first.get("candidates").get(0);
                if (candidate.has("output")) {
                    JsonNode output = candidate.get("output");
                    if (output.isTextual()) return output.asText();
                    if (output.isArray() && output.size() > 0) {
                        JsonNode part = output.get(0);
                        if (part.has("content")) return part.get("content").asText();
                    }
                }
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    JsonNode parts = candidate.get("content").get("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
            if (first.has("outputText")) {
                return first.get("outputText").asText();
            }
        }
        if (node.has("candidates") && node.get("candidates").isArray()) {
            JsonNode candidate = node.get("candidates").get(0);
            if (candidate.has("content") && candidate.get("content").has("parts")) {
                JsonNode parts = candidate.get("content").get("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).get("text").asText();
                }
            }
        }
        if (node.has("output") && node.get("output").isTextual()) {
            return node.get("output").asText();
        }
        return null;
    }

    private String callOpenAi(String prompt) {
        if (openaiKey == null || openaiKey.trim().isEmpty()) {
            return null;
        }
        try {
            log.info("Attempting OpenAI chat completion using model: {}", openaiModel);
            WebClient client = WebClient.builder().baseUrl(openaiBaseUrl).build();
            Map<String, Object> body = Map.of(
                "model", openaiModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3,
                "max_tokens", 2048
            );
            String resp = client.post().uri("/chat/completions")
                .header("Authorization", "Bearer " + openaiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body).retrieve().bodyToMono(String.class).block();
            JsonNode node = mapper.readTree(resp);
            if (node.has("choices") && node.get("choices").isArray() && node.get("choices").size() > 0) {
                return node.get("choices").get(0).get("message").get("content").asText();
            }
            log.error("OpenAI response missing choices: {}", resp);
        } catch (Exception e) {
            log.error("OpenAI call failed: {}", e.getMessage());
        }
        return null;
    }

    private float[] getOpenAiEmbedding(String text) {
        if (openaiKey == null || openaiKey.trim().isEmpty()) {
            return null;
        }
        try {
            log.info("Attempting OpenAI embedding");
            WebClient client = WebClient.builder().baseUrl(openaiBaseUrl).build();
            Map<String, Object> body = Map.of(
                "model", "text-embedding-3-small",
                "input", truncate(text, 5000),
                "dimensions", 768
            );
            String resp = client.post().uri("/embeddings")
                .header("Authorization", "Bearer " + openaiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body).retrieve().bodyToMono(String.class).block();
            JsonNode node = mapper.readTree(resp);
            if (node.has("data") && node.get("data").isArray() && node.get("data").size() > 0) {
                JsonNode embeddingNode = node.get("data").get(0).get("embedding");
                if (embeddingNode != null && embeddingNode.isArray()) {
                    return jsonArrayToFloatArray(embeddingNode);
                }
            }
            log.error("OpenAI embedding response missing expected structure: {}", resp);
        } catch (Exception e) {
            log.error("OpenAI embedding failed: {}", e.getMessage());
        }
        return null;
    }

    private String getLocalMockResponse(String prompt) {
        String lower = prompt.toLowerCase();
        
        // Check if categorization query
        if (lower.contains("classify this email into exactly one category") || lower.contains("category")) {
            String category = "Work/Professional";
            if (lower.contains("newsletter") || lower.contains("subscribe")) {
                category = "Newsletters";
            } else if (lower.contains("invoice") || lower.contains("receipt") || lower.contains("billing") || lower.contains("payment") || lower.contains("finance") || lower.contains("bank")) {
                category = "Finance";
            } else if (lower.contains("job") || lower.contains("career") || lower.contains("resume") || lower.contains("recruit") || lower.contains("interview") || lower.contains("university") || lower.contains("admissions")) {
                category = "Job/Recruitment";
            } else if (lower.contains("alert") || lower.contains("notification") || lower.contains("security") || lower.contains("verification") || lower.contains("code")) {
                category = "Notifications";
            } else if (lower.contains("personal") || lower.contains("family") || lower.contains("friend") || lower.contains("dinner") || lower.contains("party")) {
                category = "Personal";
            }
            return "{\"category\": \"" + category + "\", \"confidence\": 0.95}";
        }
        
        // Check if search plan query
        if (lower.contains("json search plan that filters emails")) {
            return "{\n"
                + "  \"answerScope\": \"MAIL\",\n"
                + "  \"sender\": null,\n"
                + "  \"subject\": null,\n"
                + "  \"timeRangeDays\": null,\n"
                + "  \"category\": null,\n"
                + "  \"keywords\": [],\n"
                + "  \"useVectorSearch\": false,\n"
                + "  \"vectorSearchQuery\": null\n"
                + "}";
        }

        // Check if summarize query
        if (lower.contains("summarize")) {
            String subject = "Email";
            if (prompt.contains("Subject: ")) {
                int start = prompt.indexOf("Subject: ") + 9;
                int end = prompt.indexOf("\n", start);
                if (end > start) {
                    subject = prompt.substring(start, end).trim();
                }
            }
            String from = "";
            if (prompt.contains("From: ")) {
                int start = prompt.indexOf("From: ") + 6;
                int end = prompt.indexOf("\n", start);
                if (end > start) {
                    from = prompt.substring(start, end).trim();
                }
            }
            String senderStr = from.isEmpty() ? "" : " from " + from;
            return "This email" + senderStr + " regarding \"" + subject + "\" contains details about current updates. Key point: Please review any attachments or follow up as appropriate.";
        }

        // Check if draft/reply query
        if (lower.contains("generate a professional email") || lower.contains("reply")) {
            return "Dear Recipient,\n\nThank you for your message. I have received your email and will review the details shortly. I will get back to you with updates as soon as possible.\n\nBest regards,\nJaswanth";
        }

        // General fallback
        return "I have reviewed your query and emails. However, the connection is currently operating in offline mode. Let me know if you would like me to assist you with drafting any email replies.";
    }

    private String truncate(String text, int maxLen) {
        return text == null ? "" : (text.length() > maxLen ? text.substring(0, maxLen) + "..." : text);
    }
}
