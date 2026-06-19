package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(nullable = false)
    private String role; // 'user' or 'assistant'

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "source_email_ids")
    private UUID[] sourceEmailIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_metadata")
    private String sourceMetadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(UUID id, ChatSession session, String role, String content, UUID[] sourceEmailIds, String sourceMetadata, Instant createdAt) {
        this.id = id;
        this.session = session;
        this.role = role;
        this.content = content;
        this.sourceEmailIds = sourceEmailIds;
        this.sourceMetadata = sourceMetadata;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ChatSession getSession() { return session; }
    public void setSession(ChatSession session) { this.session = session; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public UUID[] getSourceEmailIds() { return sourceEmailIds; }
    public void setSourceEmailIds(UUID[] sourceEmailIds) { this.sourceEmailIds = sourceEmailIds; }

    public String getSourceMetadata() { return sourceMetadata; }
    public void setSourceMetadata(String sourceMetadata) { this.sourceMetadata = sourceMetadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Manual Builder
    public static class ChatMessageBuilder {
        private ChatSession session;
        private String role;
        private String content;
        private UUID[] sourceEmailIds;
        private String sourceMetadata;

        public ChatMessageBuilder session(ChatSession session) { this.session = session; return this; }
        public ChatMessageBuilder role(String role) { this.role = role; return this; }
        public ChatMessageBuilder content(String content) { this.content = content; return this; }
        public ChatMessageBuilder sourceEmailIds(UUID[] sourceEmailIds) { this.sourceEmailIds = sourceEmailIds; return this; }
        public ChatMessageBuilder sourceMetadata(String sourceMetadata) { this.sourceMetadata = sourceMetadata; return this; }

        public ChatMessage build() {
            ChatMessage cm = new ChatMessage();
            cm.setSession(session);
            cm.setRole(role);
            cm.setContent(content);
            cm.setSourceEmailIds(sourceEmailIds);
            cm.setSourceMetadata(sourceMetadata);
            return cm;
        }
    }

    public static ChatMessageBuilder builder() {
        return new ChatMessageBuilder();
    }
}
