package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drafts")
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gmail_account_id", nullable = false)
    private GmailAccount gmailAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_reply_to_email_id")
    private Email inReplyToEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_reply_to_thread_id")
    private EmailThread inReplyToThread;

    @Column(name = "recipient_emails")
    private String[] recipientEmails;

    @Column(name = "cc_emails")
    private String[] ccEmails;

    @Column(name = "subject", length = 1000)
    private String subject;

    @Column(name = "body_text", columnDefinition = "text")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "text")
    private String bodyHtml;

    @Column(name = "user_prompt", columnDefinition = "text")
    private String userPrompt;

    @Column(name = "ai_model_used")
    private String aiModelUsed;

    @Column(name = "is_sent")
    private Boolean isSent = false;

    @Column(name = "gmail_draft_id")
    private String gmailDraftId;

    @Column(name = "gmail_message_id")
    private String gmailMessageId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public Draft() {}

    public Draft(UUID id, GmailAccount gmailAccount, Email inReplyToEmail, EmailThread inReplyToThread,
                 String[] recipientEmails, String[] ccEmails, String subject, String bodyText, String bodyHtml,
                 String userPrompt, String aiModelUsed, Boolean isSent, String gmailDraftId, String gmailMessageId,
                 Instant sentAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.gmailAccount = gmailAccount;
        this.inReplyToEmail = inReplyToEmail;
        this.inReplyToThread = inReplyToThread;
        this.recipientEmails = recipientEmails;
        this.ccEmails = ccEmails;
        this.subject = subject;
        this.bodyText = bodyText;
        this.bodyHtml = bodyHtml;
        this.userPrompt = userPrompt;
        this.aiModelUsed = aiModelUsed;
        this.isSent = isSent;
        this.gmailDraftId = gmailDraftId;
        this.gmailMessageId = gmailMessageId;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GmailAccount getGmailAccount() { return gmailAccount; }
    public void setGmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; }

    public Email getInReplyToEmail() { return inReplyToEmail; }
    public void setInReplyToEmail(Email inReplyToEmail) { this.inReplyToEmail = inReplyToEmail; }

    public EmailThread getInReplyToThread() { return inReplyToThread; }
    public void setInReplyToThread(EmailThread inReplyToThread) { this.inReplyToThread = inReplyToThread; }

    public String[] getRecipientEmails() { return recipientEmails; }
    public void setRecipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; }

    public String[] getCcEmails() { return ccEmails; }
    public void setCcEmails(String[] ccEmails) { this.ccEmails = ccEmails; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public String getAiModelUsed() { return aiModelUsed; }
    public void setAiModelUsed(String aiModelUsed) { this.aiModelUsed = aiModelUsed; }

    public Boolean getIsSent() { return isSent; }
    public void setIsSent(Boolean isSent) { this.isSent = isSent; }

    public String getGmailDraftId() { return gmailDraftId; }
    public void setGmailDraftId(String gmailDraftId) { this.gmailDraftId = gmailDraftId; }

    public String getGmailMessageId() { return gmailMessageId; }
    public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static class DraftBuilder {
        private GmailAccount gmailAccount;
        private Email inReplyToEmail;
        private EmailThread inReplyToThread;
        private String[] recipientEmails;
        private String[] ccEmails;
        private String subject;
        private String bodyText;
        private String bodyHtml;
        private String userPrompt;
        private String aiModelUsed;
        private Boolean isSent = false;
        private String gmailDraftId;
        private String gmailMessageId;
        private Instant sentAt;

        public DraftBuilder gmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; return this; }
        public DraftBuilder inReplyToEmail(Email inReplyToEmail) { this.inReplyToEmail = inReplyToEmail; return this; }
        public DraftBuilder inReplyToThread(EmailThread inReplyToThread) { this.inReplyToThread = inReplyToThread; return this; }
        public DraftBuilder recipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; return this; }
        public DraftBuilder ccEmails(String[] ccEmails) { this.ccEmails = ccEmails; return this; }
        public DraftBuilder subject(String subject) { this.subject = subject; return this; }
        public DraftBuilder bodyText(String bodyText) { this.bodyText = bodyText; return this; }
        public DraftBuilder bodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; return this; }
        public DraftBuilder userPrompt(String userPrompt) { this.userPrompt = userPrompt; return this; }
        public DraftBuilder aiModelUsed(String aiModelUsed) { this.aiModelUsed = aiModelUsed; return this; }
        public DraftBuilder isSent(Boolean isSent) { this.isSent = isSent; return this; }
        public DraftBuilder gmailDraftId(String gmailDraftId) { this.gmailDraftId = gmailDraftId; return this; }
        public DraftBuilder gmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; return this; }
        public DraftBuilder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }

        public Draft build() {
            Draft d = new Draft();
            d.setGmailAccount(gmailAccount);
            d.setInReplyToEmail(inReplyToEmail);
            d.setInReplyToThread(inReplyToThread);
            d.setRecipientEmails(recipientEmails);
            d.setCcEmails(ccEmails);
            d.setSubject(subject);
            d.setBodyText(bodyText);
            d.setBodyHtml(bodyHtml);
            d.setUserPrompt(userPrompt);
            d.setAiModelUsed(aiModelUsed);
            d.setIsSent(isSent);
            d.setGmailDraftId(gmailDraftId);
            d.setGmailMessageId(gmailMessageId);
            d.setSentAt(sentAt);
            return d;
        }
    }

    public static DraftBuilder builder() {
        return new DraftBuilder();
    }
}
