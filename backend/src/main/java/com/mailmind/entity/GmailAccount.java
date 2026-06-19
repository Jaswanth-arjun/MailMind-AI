package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gmail_accounts")
public class GmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "gmail_email", nullable = false)
    private String gmailEmail;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "scopes", columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "connected_at")
    private Instant connectedAt;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public GmailAccount() {
    }

    public GmailAccount(UUID id, User user, String gmailEmail, String accessToken, String refreshToken,
            Instant tokenExpiresAt, String scopes, Instant connectedAt, Instant lastSyncAt,
            Boolean isActive, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.gmailEmail = gmailEmail;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.scopes = scopes;
        this.connectedAt = connectedAt;
        this.lastSyncAt = lastSyncAt;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGmailEmail() {
        return gmailEmail;
    }

    public void setGmailEmail(String gmailEmail) {
        this.gmailEmail = gmailEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(Instant lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Manual Builder
    public static class GmailAccountBuilder {
        private User user;
        private String gmailEmail;
        private String accessToken;
        private String refreshToken;
        private Instant tokenExpiresAt;
        private String scopes;
        private Instant connectedAt;
        private Instant lastSyncAt;
        private Boolean isActive = true;

        public GmailAccountBuilder user(User user) {
            this.user = user;
            return this;
        }

        public GmailAccountBuilder gmailEmail(String gmailEmail) {
            this.gmailEmail = gmailEmail;
            return this;
        }

        public GmailAccountBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public GmailAccountBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public GmailAccountBuilder tokenExpiresAt(Instant tokenExpiresAt) {
            this.tokenExpiresAt = tokenExpiresAt;
            return this;
        }

        public GmailAccountBuilder scopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        public GmailAccountBuilder connectedAt(Instant connectedAt) {
            this.connectedAt = connectedAt;
            return this;
        }

        public GmailAccountBuilder lastSyncAt(Instant lastSyncAt) {
            this.lastSyncAt = lastSyncAt;
            return this;
        }

        public GmailAccountBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public GmailAccount build() {
            GmailAccount ga = new GmailAccount();
            ga.setUser(user);
            ga.setGmailEmail(gmailEmail);
            ga.setAccessToken(accessToken);
            ga.setRefreshToken(refreshToken);
            ga.setTokenExpiresAt(tokenExpiresAt);
            ga.setScopes(scopes);
            ga.setConnectedAt(connectedAt);
            ga.setLastSyncAt(lastSyncAt);
            ga.setIsActive(isActive);
            return ga;
        }
    }

    public static GmailAccountBuilder builder() {
        return new GmailAccountBuilder();
    }
}
