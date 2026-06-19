package com.mailmind.controller;

import com.mailmind.ai.AiService;
import com.mailmind.gmail.GmailService;
import com.mailmind.dto.ApiDtos.*;
import com.mailmind.entity.*;
import com.mailmind.rag.RagService;
import com.mailmind.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AiController {

    private final AiService aiService;
    private final RagService ragService;
    private final EmailRepository emailRepo;
    private final ThreadRepository threadRepo;
    private final GmailAccountRepository gmailAccountRepo;
    private final DraftRepository draftRepo;
    private final GmailService gmailService;

    public AiController(AiService ai, RagService rag, EmailRepository er, ThreadRepository tr,
                         GmailAccountRepository gar, DraftRepository dr, GmailService gs) {
        this.aiService = ai; this.ragService = rag; this.emailRepo = er;
        this.threadRepo = tr; this.gmailAccountRepo = gar; this.draftRepo = dr; this.gmailService = gs;
    }

    @PostMapping("/ai/summarize/email/{emailId}")
    public ResponseEntity<SummaryResponse> summarizeEmail(@PathVariable UUID emailId) {
        Email email = emailRepo.findById(emailId).orElseThrow();
        String summary = aiService.summarizeEmail(email.getBodyText(), email.getSubject(), email.getSenderEmail());
        email.setAiSummary(summary); email.setAiSummaryGeneratedAt(Instant.now());
        emailRepo.save(email);
        // Also create embedding
        ragService.embedEmail(email);
        return ResponseEntity.ok(SummaryResponse.builder().entityId(emailId).summary(summary).modelUsed("gemini").build());
    }

    @PostMapping("/ai/summarize/thread/{threadId}")
    public ResponseEntity<SummaryResponse> summarizeThread(@PathVariable String threadId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        EmailThread thread = threadRepo.findByGmailAccountIdAndGmailThreadId(acct.getId(), threadId).orElseThrow();
        List<Email> msgs = emailRepo.findByGmailAccountIdAndGmailThreadIdOrderByReceivedAtAsc(acct.getId(), threadId);
        List<Map<String, String>> msgCtx = msgs.stream().map(m -> Map.of(
            "sender", m.getSenderEmail() != null ? m.getSenderEmail() : "",
            "date", m.getReceivedAt() != null ? m.getReceivedAt().toString() : "",
            "body", m.getBodyText() != null ? m.getBodyText() : ""
        )).collect(Collectors.toList());
        String summary = aiService.summarizeThread(msgCtx);
        thread.setAiSummary(summary); thread.setAiSummaryGeneratedAt(Instant.now());
        threadRepo.save(thread);
        return ResponseEntity.ok(SummaryResponse.builder().entityId(thread.getId()).summary(summary).modelUsed("gemini").build());
    }

    @PostMapping("/ai/categorize/{emailId}")
    public ResponseEntity<CategoryResponse> categorize(@PathVariable UUID emailId) {
        Email email = emailRepo.findById(emailId).orElseThrow();
        Map<String, Object> result = aiService.categorizeEmail(email.getBodyText(), email.getSubject(), email.getSenderEmail());
        email.setAiCategory((String) result.get("category"));
        email.setAiCategoryConfidence(((Number) result.get("confidence")).floatValue());
        email.setAiCategorizedAt(Instant.now());
        emailRepo.save(email);
        return ResponseEntity.ok(CategoryResponse.builder().emailId(emailId)
            .category(email.getAiCategory()).confidence(email.getAiCategoryConfidence()).modelUsed("gemini").build());
    }

    @PostMapping("/chat/query")
    public ResponseEntity<ChatQueryResponse> chat(@RequestBody ChatQueryRequest req, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        ChatQueryResponse resp = ragService.chat(userId, acct.getId(), req.getQuestion(), req.getSessionId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/drafts/generate")
    public ResponseEntity<DraftResponse> generateDraft(@RequestBody GenerateDraftRequest req, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        String body = aiService.generateDraft(req.getPrompt(),
            req.getRecipientEmails() != null ? String.join(", ", req.getRecipientEmails()) : null);
        Draft draft = draftRepo.save(Draft.builder().gmailAccount(acct).recipientEmails(req.getRecipientEmails())
            .subject(req.getSubject()).bodyText(body).userPrompt(req.getPrompt()).aiModelUsed("gemini").build());
        return ResponseEntity.ok(DraftResponse.builder().draftId(draft.getId()).subject(req.getSubject())
            .bodyText(body).recipientEmails(req.getRecipientEmails()).modelUsed("gemini").build());
    }

    @PostMapping("/reply/generate")
    public ResponseEntity<ReplyResponse> generateReply(@RequestBody GenerateReplyRequest req, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        List<Email> msgs = emailRepo.findByGmailAccountIdAndGmailThreadIdOrderByReceivedAtAsc(acct.getId(), req.getThreadId());
        List<Map<String, String>> ctx = msgs.stream().map(m -> Map.of(
            "sender", m.getSenderEmail() != null ? m.getSenderEmail() : "",
            "date", m.getReceivedAt() != null ? m.getReceivedAt().toString() : "",
            "body", m.getBodyText() != null ? m.getBodyText() : ""
        )).collect(Collectors.toList());
        String body = aiService.generateReply(req.getInstruction(), ctx);
        Email replyTo = req.getEmailId() != null ? emailRepo.findById(req.getEmailId()).orElse(null) : null;
        Draft draft = draftRepo.save(Draft.builder().gmailAccount(acct)
            .inReplyToEmail(replyTo).subject("Re: " + (msgs.isEmpty() ? "" : msgs.get(0).getSubject()))
            .bodyText(body).userPrompt(req.getInstruction()).aiModelUsed("gemini").build());
        return ResponseEntity.ok(ReplyResponse.builder().draftId(draft.getId())
            .subject(draft.getSubject()).bodyText(body).modelUsed("gemini").build());
    }

    @PostMapping("/drafts/send")
    public ResponseEntity<SendResponse> sendDraft(@RequestBody SendDraftRequest req, Authentication auth) throws Exception {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        Draft draft = draftRepo.findById(req.getDraftId()).orElseThrow(() -> new RuntimeException("Draft not found"));
        
        String to = req.getRecipientEmails() != null && req.getRecipientEmails().length > 0 
            ? String.join(",", req.getRecipientEmails()) 
            : (draft.getRecipientEmails() != null ? String.join(",", draft.getRecipientEmails()) : "");
            
        String subject = req.getSubject() != null ? req.getSubject() : draft.getSubject();
        String body = req.getBodyText() != null ? req.getBodyText() : draft.getBodyText();
        
        String msgId = gmailService.sendEmail(acct, to, subject, body, null, null);
        
        draft.setIsSent(true);
        draft.setSentAt(Instant.now());
        draft.setGmailMessageId(msgId);
        draft.setRecipientEmails(req.getRecipientEmails() != null ? req.getRecipientEmails() : draft.getRecipientEmails());
        draft.setSubject(subject);
        draft.setBodyText(body);
        draftRepo.save(draft);
        
        return ResponseEntity.ok(SendResponse.builder().success(true).gmailMessageId(msgId).message("Draft sent successfully").build());
    }

    @PostMapping("/reply/send")
    public ResponseEntity<SendResponse> sendReply(@RequestBody SendDraftRequest req, Authentication auth) throws Exception {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        Draft draft = draftRepo.findById(req.getDraftId()).orElseThrow(() -> new RuntimeException("Draft not found"));
        
        String to = req.getRecipientEmails() != null && req.getRecipientEmails().length > 0 
            ? String.join(",", req.getRecipientEmails()) 
            : (draft.getRecipientEmails() != null ? String.join(",", draft.getRecipientEmails()) : "");
            
        if ((to == null || to.isEmpty()) && draft.getInReplyToEmail() != null) {
            to = draft.getInReplyToEmail().getSenderEmail();
        }
            
        String subject = req.getSubject() != null ? req.getSubject() : draft.getSubject();
        String body = req.getBodyText() != null ? req.getBodyText() : draft.getBodyText();
        
        String replyToMsgId = draft.getInReplyToEmail() != null ? draft.getInReplyToEmail().getGmailMessageId() : null;
        String threadId = draft.getInReplyToEmail() != null ? draft.getInReplyToEmail().getGmailThreadId() : null;
        
        String msgId = gmailService.sendEmail(acct, to, subject, body, replyToMsgId, threadId);
        
        draft.setIsSent(true);
        draft.setSentAt(Instant.now());
        draft.setGmailMessageId(msgId);
        draft.setRecipientEmails(req.getRecipientEmails() != null ? req.getRecipientEmails() : new String[]{to});
        draft.setSubject(subject);
        draft.setBodyText(body);
        draftRepo.save(draft);
        
        return ResponseEntity.ok(SendResponse.builder().success(true).gmailMessageId(msgId).message("Reply sent successfully").build());
    }
}

