package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gmail_account_id", nullable = false)
    private GmailAccount gmailAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private EmailThread thread;

    @Column(name = "gmail_message_id", nullable = false)
    private String gmailMessageId;

    @Column(name = "gmail_thread_id", nullable = false)
    private String gmailThreadId;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "recipient_emails")
    private String[] recipientEmails;

    @Column(name = "cc_emails")
    private String[] ccEmails;

    @Column(name = "bcc_emails")
    private String[] bccEmails;

    @Column(name = "subject", length = 1000)
    private String subject;

    @Column(columnDefinition = "text")
    private String snippet;

    @Column(name = "body_text", columnDefinition = "text")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "text")
    private String bodyHtml;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "internal_date")
    private Long internalDate;

    @Column(name = "size_estimate")
    private Integer sizeEstimate;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_starred")
    private Boolean isStarred = false;

    @Column(name = "is_draft")
    private Boolean isDraft = false;

    @Column(name = "has_attachments")
    private Boolean hasAttachments = false;

    @Column(name = "in_inbox")
    private Boolean inInbox = true;

    @Column(name = "gmail_label_ids")
    private String[] gmailLabelIds;

    // AI-generated fields
    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Column(name = "ai_summary_generated_at")
    private Instant aiSummaryGeneratedAt;

    @Column(name = "ai_category")
    private String aiCategory;

    @Column(name = "ai_category_confidence")
    private Float aiCategoryConfidence;

    @Column(name = "ai_categorized_at")
    private Instant aiCategorizedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public Email() {}

    public Email(UUID id, GmailAccount gmailAccount, EmailThread thread, String gmailMessageId, String gmailThreadId,
                 String senderEmail, String senderName, String[] recipientEmails, String[] ccEmails, String[] bccEmails,
                 String subject, String snippet, String bodyText, String bodyHtml, Instant receivedAt, Long internalDate,
                 Integer sizeEstimate, Boolean isRead, Boolean isStarred, Boolean isDraft, Boolean hasAttachments,
                 Boolean inInbox, String[] gmailLabelIds, String aiSummary, Instant aiSummaryGeneratedAt, String aiCategory,
                 Float aiCategoryConfidence, Instant aiCategorizedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.gmailAccount = gmailAccount;
        this.thread = thread;
        this.gmailMessageId = gmailMessageId;
        this.gmailThreadId = gmailThreadId;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.recipientEmails = recipientEmails;
        this.ccEmails = ccEmails;
        this.bccEmails = bccEmails;
        this.subject = subject;
        this.snippet = snippet;
        this.bodyText = bodyText;
        this.bodyHtml = bodyHtml;
        this.receivedAt = receivedAt;
        this.internalDate = internalDate;
        this.sizeEstimate = sizeEstimate;
        this.isRead = isRead;
        this.isStarred = isStarred;
        this.isDraft = isDraft;
        this.hasAttachments = hasAttachments;
        this.inInbox = inInbox;
        this.gmailLabelIds = gmailLabelIds;
        this.aiSummary = aiSummary;
        this.aiSummaryGeneratedAt = aiSummaryGeneratedAt;
        this.aiCategory = aiCategory;
        this.aiCategoryConfidence = aiCategoryConfidence;
        this.aiCategorizedAt = aiCategorizedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GmailAccount getGmailAccount() { return gmailAccount; }
    public void setGmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; }

    public EmailThread getThread() { return thread; }
    public void setThread(EmailThread thread) { this.thread = thread; }

    public String getGmailMessageId() { return gmailMessageId; }
    public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }

    public String getGmailThreadId() { return gmailThreadId; }
    public void setGmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String[] getRecipientEmails() { return recipientEmails; }
    public void setRecipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; }

    public String[] getCcEmails() { return ccEmails; }
    public void setCcEmails(String[] ccEmails) { this.ccEmails = ccEmails; }

    public String[] getBccEmails() { return bccEmails; }
    public void setBccEmails(String[] bccEmails) { this.bccEmails = bccEmails; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public Long getInternalDate() { return internalDate; }
    public void setInternalDate(Long internalDate) { this.internalDate = internalDate; }

    public Integer getSizeEstimate() { return sizeEstimate; }
    public void setSizeEstimate(Integer sizeEstimate) { this.sizeEstimate = sizeEstimate; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Boolean getIsStarred() { return isStarred; }
    public void setIsStarred(Boolean isStarred) { this.isStarred = isStarred; }

    public Boolean getIsDraft() { return isDraft; }
    public void setIsDraft(Boolean isDraft) { this.isDraft = isDraft; }

    public Boolean getHasAttachments() { return hasAttachments; }
    public void setHasAttachments(Boolean hasAttachments) { this.hasAttachments = hasAttachments; }

    public Boolean getInInbox() { return inInbox; }
    public void setInInbox(Boolean inInbox) { this.inInbox = inInbox; }

    public String[] getGmailLabelIds() { return gmailLabelIds; }
    public void setGmailLabelIds(String[] gmailLabelIds) { this.gmailLabelIds = gmailLabelIds; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public Instant getAiSummaryGeneratedAt() { return aiSummaryGeneratedAt; }
    public void setAiSummaryGeneratedAt(Instant aiSummaryGeneratedAt) { this.aiSummaryGeneratedAt = aiSummaryGeneratedAt; }

    public String getAiCategory() { return aiCategory; }
    public void setAiCategory(String aiCategory) { this.aiCategory = aiCategory; }

    public Float getAiCategoryConfidence() { return aiCategoryConfidence; }
    public void setAiCategoryConfidence(Float aiCategoryConfidence) { this.aiCategoryConfidence = aiCategoryConfidence; }

    public Instant getAiCategorizedAt() { return aiCategorizedAt; }
    public void setAiCategorizedAt(Instant aiCategorizedAt) { this.aiCategorizedAt = aiCategorizedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static class EmailBuilder {
        private GmailAccount gmailAccount;
        private EmailThread thread;
        private String gmailMessageId;
        private String gmailThreadId;
        private String senderEmail;
        private String senderName;
        private String[] recipientEmails;
        private String[] ccEmails;
        private String[] bccEmails;
        private String subject;
        private String snippet;
        private String bodyText;
        private String bodyHtml;
        private Instant receivedAt;
        private Long internalDate;
        private Integer sizeEstimate;
        private Boolean isRead = false;
        private Boolean isStarred = false;
        private Boolean isDraft = false;
        private Boolean hasAttachments = false;
        private Boolean inInbox = true;
        private String[] gmailLabelIds;
        private String aiSummary;
        private Instant aiSummaryGeneratedAt;
        private String aiCategory;
        private Float aiCategoryConfidence;
        private Instant aiCategorizedAt;

        public EmailBuilder gmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; return this; }
        public EmailBuilder thread(EmailThread thread) { this.thread = thread; return this; }
        public EmailBuilder gmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; return this; }
        public EmailBuilder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
        public EmailBuilder senderEmail(String senderEmail) { this.senderEmail = senderEmail; return this; }
        public EmailBuilder senderName(String senderName) { this.senderName = senderName; return this; }
        public EmailBuilder recipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; return this; }
        public EmailBuilder ccEmails(String[] ccEmails) { this.ccEmails = ccEmails; return this; }
        public EmailBuilder bccEmails(String[] bccEmails) { this.bccEmails = bccEmails; return this; }
        public EmailBuilder subject(String subject) { this.subject = subject; return this; }
        public EmailBuilder snippet(String snippet) { this.snippet = snippet; return this; }
        public EmailBuilder bodyText(String bodyText) { this.bodyText = bodyText; return this; }
        public EmailBuilder bodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; return this; }
        public EmailBuilder receivedAt(Instant receivedAt) { this.receivedAt = receivedAt; return this; }
        public EmailBuilder internalDate(Long internalDate) { this.internalDate = internalDate; return this; }
        public EmailBuilder sizeEstimate(Integer sizeEstimate) { this.sizeEstimate = sizeEstimate; return this; }
        public EmailBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public EmailBuilder isStarred(Boolean isStarred) { this.isStarred = isStarred; return this; }
        public EmailBuilder isDraft(Boolean isDraft) { this.isDraft = isDraft; return this; }
        public EmailBuilder hasAttachments(Boolean hasAttachments) { this.hasAttachments = hasAttachments; return this; }
        public EmailBuilder inInbox(Boolean inInbox) { this.inInbox = inInbox; return this; }
        public EmailBuilder gmailLabelIds(String[] gmailLabelIds) { this.gmailLabelIds = gmailLabelIds; return this; }
        public EmailBuilder aiSummary(String aiSummary) { this.aiSummary = aiSummary; return this; }
        public EmailBuilder aiSummaryGeneratedAt(Instant aiSummaryGeneratedAt) { this.aiSummaryGeneratedAt = aiSummaryGeneratedAt; return this; }
        public EmailBuilder aiCategory(String aiCategory) { this.aiCategory = aiCategory; return this; }
        public EmailBuilder aiCategoryConfidence(Float aiCategoryConfidence) { this.aiCategoryConfidence = aiCategoryConfidence; return this; }
        public EmailBuilder aiCategorizedAt(Instant aiCategorizedAt) { this.aiCategorizedAt = aiCategorizedAt; return this; }

        public Email build() {
            Email e = new Email();
            e.setGmailAccount(gmailAccount);
            e.setThread(thread);
            e.setGmailMessageId(gmailMessageId);
            e.setGmailThreadId(gmailThreadId);
            e.setSenderEmail(senderEmail);
            e.setSenderName(senderName);
            e.setRecipientEmails(recipientEmails);
            e.setCcEmails(ccEmails);
            e.setBccEmails(bccEmails);
            e.setSubject(subject);
            e.setSnippet(snippet);
            e.setBodyText(bodyText);
            e.setBodyHtml(bodyHtml);
            e.setReceivedAt(receivedAt);
            e.setInternalDate(internalDate);
            e.setSizeEstimate(sizeEstimate);
            e.setIsRead(isRead);
            e.setIsStarred(isStarred);
            e.setIsDraft(isDraft);
            e.setHasAttachments(hasAttachments);
            e.setInInbox(inInbox);
            e.setGmailLabelIds(gmailLabelIds);
            e.setAiSummary(aiSummary);
            e.setAiSummaryGeneratedAt(aiSummaryGeneratedAt);
            e.setAiCategory(aiCategory);
            e.setAiCategoryConfidence(aiCategoryConfidence);
            e.setAiCategorizedAt(aiCategorizedAt);
            return e;
        }
    }

    public static EmailBuilder builder() {
        return new EmailBuilder();
    }
}
