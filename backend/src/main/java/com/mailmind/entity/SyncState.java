package com.mailmind.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_state")
public class SyncState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gmail_account_id", nullable = false, unique = true)
    private GmailAccount gmailAccount;

    @Column(name = "last_history_id")
    private Long lastHistoryId;

    @Column(name = "last_sync_started_at")
    private Instant lastSyncStartedAt;

    @Column(name = "last_sync_completed_at")
    private Instant lastSyncCompletedAt;

    @Column(name = "sync_status")
    private String syncStatus = "IDLE";

    @Column(name = "total_messages_synced")
    private Integer totalMessagesSynced = 0;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "page_token")
    private String pageToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public SyncState() {}

    public SyncState(UUID id, GmailAccount gmailAccount, Long lastHistoryId, Instant lastSyncStartedAt,
                     Instant lastSyncCompletedAt, String syncStatus, Integer totalMessagesSynced,
                     String errorMessage, String pageToken, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.gmailAccount = gmailAccount;
        this.lastHistoryId = lastHistoryId;
        this.lastSyncStartedAt = lastSyncStartedAt;
        this.lastSyncCompletedAt = lastSyncCompletedAt;
        this.syncStatus = syncStatus;
        this.totalMessagesSynced = totalMessagesSynced;
        this.errorMessage = errorMessage;
        this.pageToken = pageToken;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GmailAccount getGmailAccount() { return gmailAccount; }
    public void setGmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; }

    public Long getLastHistoryId() { return lastHistoryId; }
    public void setLastHistoryId(Long lastHistoryId) { this.lastHistoryId = lastHistoryId; }

    public Instant getLastSyncStartedAt() { return lastSyncStartedAt; }
    public void setLastSyncStartedAt(Instant lastSyncStartedAt) { this.lastSyncStartedAt = lastSyncStartedAt; }

    public Instant getLastSyncCompletedAt() { return lastSyncCompletedAt; }
    public void setLastSyncCompletedAt(Instant lastSyncCompletedAt) { this.lastSyncCompletedAt = lastSyncCompletedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public Integer getTotalMessagesSynced() { return totalMessagesSynced; }
    public void setTotalMessagesSynced(Integer totalMessagesSynced) { this.totalMessagesSynced = totalMessagesSynced; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getPageToken() { return pageToken; }
    public void setPageToken(String pageToken) { this.pageToken = pageToken; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static class SyncStateBuilder {
        private GmailAccount gmailAccount;
        private Long lastHistoryId;
        private Instant lastSyncStartedAt;
        private Instant lastSyncCompletedAt;
        private String syncStatus = "IDLE";
        private Integer totalMessagesSynced = 0;
        private String errorMessage;
        private String pageToken;

        public SyncStateBuilder gmailAccount(GmailAccount gmailAccount) { this.gmailAccount = gmailAccount; return this; }
        public SyncStateBuilder lastHistoryId(Long lastHistoryId) { this.lastHistoryId = lastHistoryId; return this; }
        public SyncStateBuilder lastSyncStartedAt(Instant lastSyncStartedAt) { this.lastSyncStartedAt = lastSyncStartedAt; return this; }
        public SyncStateBuilder lastSyncCompletedAt(Instant lastSyncCompletedAt) { this.lastSyncCompletedAt = lastSyncCompletedAt; return this; }
        public SyncStateBuilder syncStatus(String syncStatus) { this.syncStatus = syncStatus; return this; }
        public SyncStateBuilder totalMessagesSynced(Integer totalMessagesSynced) { this.totalMessagesSynced = totalMessagesSynced; return this; }
        public SyncStateBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public SyncStateBuilder pageToken(String pageToken) { this.pageToken = pageToken; return this; }

        public SyncState build() {
            SyncState s = new SyncState();
            s.setGmailAccount(gmailAccount);
            s.setLastHistoryId(lastHistoryId);
            s.setLastSyncStartedAt(lastSyncStartedAt);
            s.setLastSyncCompletedAt(lastSyncCompletedAt);
            s.setSyncStatus(syncStatus);
            s.setTotalMessagesSynced(totalMessagesSynced);
            s.setErrorMessage(errorMessage);
            s.setPageToken(pageToken);
            return s;
        }
    }

    public static SyncStateBuilder builder() {
        return new SyncStateBuilder();
    }
}
