package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gmail_account_id", nullable = false)
    private GmailAccount gmailAccount;

    private String title;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructors
    public ChatSession() {}

    public ChatSession(UUID id, User user, GmailAccount gmailAccount, String title, Boolean isActive, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.gmailAccount = gmailAccount;
        this.title = title;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public GmailAccount getGmailAccount() { return gmailAccount; }
    public void setGmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static class ChatSessionBuilder {
        private User user;
        private GmailAccount gmailAccount;
        private String title;
        private Boolean isActive = true;

        public ChatSessionBuilder user(User user) { this.user = user; return this; }
        public ChatSessionBuilder gmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; return this; }
        public ChatSessionBuilder title(String title) { this.title = title; return this; }
        public ChatSessionBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }

        public ChatSession build() {
            ChatSession cs = new ChatSession();
            cs.setUser(user);
            cs.setGmailAccount(gmailAccount);
            cs.setTitle(title);
            cs.setIsActive(isActive);
            cs.setUpdatedAt(Instant.now());
            return cs;
        }
    }

    public static ChatSessionBuilder builder() {
        return new ChatSessionBuilder();
    }
}
