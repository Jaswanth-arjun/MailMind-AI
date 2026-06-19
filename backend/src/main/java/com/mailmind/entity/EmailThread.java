package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "threads")
public class EmailThread {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gmail_account_id", nullable = false)
    private GmailAccount gmailAccount;

    @Column(name = "gmail_thread_id", nullable = false)
    private String gmailThreadId;

    @Column(name = "subject", length = 1000)
    private String subject;

    @Column(columnDefinition = "text")
    private String snippet;

    @Column(name = "message_count")
    private Integer messageCount = 0;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "participants")
    private String[] participants;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Column(name = "ai_summary_generated_at")
    private Instant aiSummaryGeneratedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public EmailThread() {}

    public EmailThread(UUID id, GmailAccount gmailAccount, String gmailThreadId, String subject, String snippet,
                       Integer messageCount, Instant lastMessageAt, String[] participants, Boolean isRead,
                       String aiSummary, Instant aiSummaryGeneratedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.gmailAccount = gmailAccount;
        this.gmailThreadId = gmailThreadId;
        this.subject = subject;
        this.snippet = snippet;
        this.messageCount = messageCount;
        this.lastMessageAt = lastMessageAt;
        this.participants = participants;
        this.isRead = isRead;
        this.aiSummary = aiSummary;
        this.aiSummaryGeneratedAt = aiSummaryGeneratedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GmailAccount getGmailAccount() { return gmailAccount; }
    public void setGmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; }

    public String getGmailThreadId() { return gmailThreadId; }
    public void setGmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }

    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String[] getParticipants() { return participants; }
    public void setParticipants(String[] participants) { this.participants = participants; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public Instant getAiSummaryGeneratedAt() { return aiSummaryGeneratedAt; }
    public void setAiSummaryGeneratedAt(Instant aiSummaryGeneratedAt) { this.aiSummaryGeneratedAt = aiSummaryGeneratedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static class EmailThreadBuilder {
        private GmailAccount gmailAccount;
        private String gmailThreadId;
        private String subject;
        private String snippet;
        private Integer messageCount = 0;
        private Instant lastMessageAt;
        private String[] participants;
        private Boolean isRead = false;
        private String aiSummary;
        private Instant aiSummaryGeneratedAt;

        public EmailThreadBuilder gmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; return this; }
        public EmailThreadBuilder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
        public EmailThreadBuilder subject(String subject) { this.subject = subject; return this; }
        public EmailThreadBuilder snippet(String snippet) { this.snippet = snippet; return this; }
        public EmailThreadBuilder messageCount(Integer messageCount) { this.messageCount = messageCount; return this; }
        public EmailThreadBuilder lastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; return this; }
        public EmailThreadBuilder participants(String[] participants) { this.participants = participants; return this; }
        public EmailThreadBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public EmailThreadBuilder aiSummary(String aiSummary) { this.aiSummary = aiSummary; return this; }
        public EmailThreadBuilder aiSummaryGeneratedAt(Instant aiSummaryGeneratedAt) { this.aiSummaryGeneratedAt = aiSummaryGeneratedAt; return this; }

        public EmailThread build() {
            EmailThread et = new EmailThread();
            et.setGmailAccount(gmailAccount);
            et.setGmailThreadId(gmailThreadId);
            et.setSubject(subject);
            et.setSnippet(snippet);
            et.setMessageCount(messageCount);
            et.setLastMessageAt(lastMessageAt);
            et.setParticipants(participants);
            et.setIsRead(isRead);
            et.setAiSummary(aiSummary);
            et.setAiSummaryGeneratedAt(aiSummaryGeneratedAt);
            return et;
        }
    }

    public static EmailThreadBuilder builder() {
        return new EmailThreadBuilder();
    }
}
