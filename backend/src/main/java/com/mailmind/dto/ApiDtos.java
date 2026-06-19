package com.mailmind.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Objects for API requests and responses.
 */
public class ApiDtos {

    // ─── Auth ───────────────────────────────────────────────
    public static class AuthUrlResponse {
        private String authUrl;

        public AuthUrlResponse() {}
        public AuthUrlResponse(String authUrl) { this.authUrl = authUrl; }

        public String getAuthUrl() { return authUrl; }
        public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

        public static class AuthUrlResponseBuilder {
            private String authUrl;
            public AuthUrlResponseBuilder authUrl(String authUrl) { this.authUrl = authUrl; return this; }
            public AuthUrlResponse build() { return new AuthUrlResponse(authUrl); }
        }
        public static AuthUrlResponseBuilder builder() { return new AuthUrlResponseBuilder(); }
    }

    public static class AuthCallbackResponse {
        private String token;
        private UserDto user;
        private boolean newUser;

        public AuthCallbackResponse() {}
        public AuthCallbackResponse(String token, UserDto user, boolean newUser) {
            this.token = token;
            this.user = user;
            this.newUser = newUser;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public UserDto getUser() { return user; }
        public void setUser(UserDto user) { this.user = user; }
        public boolean isNewUser() { return newUser; }
        public void setNewUser(boolean newUser) { this.newUser = newUser; }

        public static class AuthCallbackResponseBuilder {
            private String token;
            private UserDto user;
            private boolean newUser;
            public AuthCallbackResponseBuilder token(String token) { this.token = token; return this; }
            public AuthCallbackResponseBuilder user(UserDto user) { this.user = user; return this; }
            public AuthCallbackResponseBuilder newUser(boolean newUser) { this.newUser = newUser; return this; }
            public AuthCallbackResponse build() { return new AuthCallbackResponse(token, user, newUser); }
        }
        public static AuthCallbackResponseBuilder builder() { return new AuthCallbackResponseBuilder(); }
    }

    // ─── User ───────────────────────────────────────────────
    public static class UserDto {
        private UUID id;
        private String email;
        private String displayName;
        private String avatarUrl;
        private GmailConnectionDto gmailConnection;

        public UserDto() {}
        public UserDto(UUID id, String email, String displayName, String avatarUrl, GmailConnectionDto gmailConnection) {
            this.id = id;
            this.email = email;
            this.displayName = displayName;
            this.avatarUrl = avatarUrl;
            this.gmailConnection = gmailConnection;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public GmailConnectionDto getGmailConnection() { return gmailConnection; }
        public void setGmailConnection(GmailConnectionDto gmailConnection) { this.gmailConnection = gmailConnection; }

        public static class UserDtoBuilder {
            private UUID id;
            private String email;
            private String displayName;
            private String avatarUrl;
            private GmailConnectionDto gmailConnection;
            public UserDtoBuilder id(UUID id) { this.id = id; return this; }
            public UserDtoBuilder email(String email) { this.email = email; return this; }
            public UserDtoBuilder displayName(String displayName) { this.displayName = displayName; return this; }
            public UserDtoBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
            public UserDtoBuilder gmailConnection(GmailConnectionDto gmailConnection) { this.gmailConnection = gmailConnection; return this; }
            public UserDto build() { return new UserDto(id, email, displayName, avatarUrl, gmailConnection); }
        }
        public static UserDtoBuilder builder() { return new UserDtoBuilder(); }
    }

    public static class GmailConnectionDto {
        private boolean connected;
        private String gmailEmail;
        private Instant lastSyncAt;
        private int totalEmailsSynced;
        private String syncStatus;
        private Long storageLimit;
        private Long storageUsage;

        public GmailConnectionDto() {}
        public GmailConnectionDto(boolean connected, String gmailEmail, Instant lastSyncAt, int totalEmailsSynced, String syncStatus, Long storageLimit, Long storageUsage) {
            this.connected = connected;
            this.gmailEmail = gmailEmail;
            this.lastSyncAt = lastSyncAt;
            this.totalEmailsSynced = totalEmailsSynced;
            this.syncStatus = syncStatus;
            this.storageLimit = storageLimit;
            this.storageUsage = storageUsage;
        }

        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        public String getGmailEmail() { return gmailEmail; }
        public void setGmailEmail(String gmailEmail) { this.gmailEmail = gmailEmail; }
        public Instant getLastSyncAt() { return lastSyncAt; }
        public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
        public int getTotalEmailsSynced() { return totalEmailsSynced; }
        public void setTotalEmailsSynced(int totalEmailsSynced) { this.totalEmailsSynced = totalEmailsSynced; }
        public String getSyncStatus() { return syncStatus; }
        public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
        public Long getStorageLimit() { return storageLimit; }
        public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }
        public Long getStorageUsage() { return storageUsage; }
        public void setStorageUsage(Long storageUsage) { this.storageUsage = storageUsage; }

        public static class GmailConnectionDtoBuilder {
            private boolean connected;
            private String gmailEmail;
            private Instant lastSyncAt;
            private int totalEmailsSynced;
            private String syncStatus;
            private Long storageLimit;
            private Long storageUsage;
            public GmailConnectionDtoBuilder connected(boolean connected) { this.connected = connected; return this; }
            public GmailConnectionDtoBuilder gmailEmail(String gmailEmail) { this.gmailEmail = gmailEmail; return this; }
            public GmailConnectionDtoBuilder lastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; return this; }
            public GmailConnectionDtoBuilder totalEmailsSynced(int totalEmailsSynced) { this.totalEmailsSynced = totalEmailsSynced; return this; }
            public GmailConnectionDtoBuilder syncStatus(String syncStatus) { this.syncStatus = syncStatus; return this; }
            public GmailConnectionDtoBuilder storageLimit(Long storageLimit) { this.storageLimit = storageLimit; return this; }
            public GmailConnectionDtoBuilder storageUsage(Long storageUsage) { this.storageUsage = storageUsage; return this; }
            public GmailConnectionDto build() { return new GmailConnectionDto(connected, gmailEmail, lastSyncAt, totalEmailsSynced, syncStatus, storageLimit, storageUsage); }
        }
        public static GmailConnectionDtoBuilder builder() { return new GmailConnectionDtoBuilder(); }
    }

    // ─── Email ──────────────────────────────────────────────
    public static class EmailListResponse {
        private List<EmailSummaryDto> emails;
        private int totalCount;
        private int page;
        private int pageSize;

        public EmailListResponse() {}
        public EmailListResponse(List<EmailSummaryDto> emails, int totalCount, int page, int pageSize) {
            this.emails = emails;
            this.totalCount = totalCount;
            this.page = page;
            this.pageSize = pageSize;
        }

        public List<EmailSummaryDto> getEmails() { return emails; }
        public void setEmails(List<EmailSummaryDto> emails) { this.emails = emails; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }

        public static class EmailListResponseBuilder {
            private List<EmailSummaryDto> emails;
            private int totalCount;
            private int page;
            private int pageSize;
            public EmailListResponseBuilder emails(List<EmailSummaryDto> emails) { this.emails = emails; return this; }
            public EmailListResponseBuilder totalCount(int totalCount) { this.totalCount = totalCount; return this; }
            public EmailListResponseBuilder page(int page) { this.page = page; return this; }
            public EmailListResponseBuilder pageSize(int pageSize) { this.pageSize = pageSize; return this; }
            public EmailListResponse build() { return new EmailListResponse(emails, totalCount, page, pageSize); }
        }
        public static EmailListResponseBuilder builder() { return new EmailListResponseBuilder(); }
    }

    public static class EmailSummaryDto {
        private UUID id;
        private String gmailMessageId;
        private String gmailThreadId;
        private String senderEmail;
        private String senderName;
        private String subject;
        private String snippet;
        private Instant receivedAt;
        private boolean isRead;
        private boolean isStarred;
        private String aiSummary;
        private String aiCategory;
        private int threadMessageCount;

        public EmailSummaryDto() {}
        public EmailSummaryDto(UUID id, String gmailMessageId, String gmailThreadId, String senderEmail, String senderName,
                               String subject, String snippet, Instant receivedAt, boolean isRead, boolean isStarred,
                               String aiSummary, String aiCategory, int threadMessageCount) {
            this.id = id;
            this.gmailMessageId = gmailMessageId;
            this.gmailThreadId = gmailThreadId;
            this.senderEmail = senderEmail;
            this.senderName = senderName;
            this.subject = subject;
            this.snippet = snippet;
            this.receivedAt = receivedAt;
            this.isRead = isRead;
            this.isStarred = isStarred;
            this.aiSummary = aiSummary;
            this.aiCategory = aiCategory;
            this.threadMessageCount = threadMessageCount;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getGmailMessageId() { return gmailMessageId; }
        public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }
        public String getGmailThreadId() { return gmailThreadId; }
        public void setGmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; }
        public String getSenderEmail() { return senderEmail; }
        public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        public Instant getReceivedAt() { return receivedAt; }
        public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
        public boolean getIsRead() { return isRead; }
        public void setIsRead(boolean isRead) { this.isRead = isRead; }
        public boolean getIsStarred() { return isStarred; }
        public void setIsStarred(boolean isStarred) { this.isStarred = isStarred; }
        public String getAiSummary() { return aiSummary; }
        public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
        public String getAiCategory() { return aiCategory; }
        public void setAiCategory(String aiCategory) { this.aiCategory = aiCategory; }
        public int getThreadMessageCount() { return threadMessageCount; }
        public void setThreadMessageCount(int threadMessageCount) { this.threadMessageCount = threadMessageCount; }

        public static class EmailSummaryDtoBuilder {
            private UUID id;
            private String gmailMessageId;
            private String gmailThreadId;
            private String senderEmail;
            private String senderName;
            private String subject;
            private String snippet;
            private Instant receivedAt;
            private boolean isRead;
            private boolean isStarred;
            private String aiSummary;
            private String aiCategory;
            private int threadMessageCount;

            public EmailSummaryDtoBuilder id(UUID id) { this.id = id; return this; }
            public EmailSummaryDtoBuilder gmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; return this; }
            public EmailSummaryDtoBuilder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
            public EmailSummaryDtoBuilder senderEmail(String senderEmail) { this.senderEmail = senderEmail; return this; }
            public EmailSummaryDtoBuilder senderName(String senderName) { this.senderName = senderName; return this; }
            public EmailSummaryDtoBuilder subject(String subject) { this.subject = subject; return this; }
            public EmailSummaryDtoBuilder snippet(String snippet) { this.snippet = snippet; return this; }
            public EmailSummaryDtoBuilder receivedAt(Instant receivedAt) { this.receivedAt = receivedAt; return this; }
            public EmailSummaryDtoBuilder isRead(boolean isRead) { this.isRead = isRead; return this; }
            public EmailSummaryDtoBuilder isStarred(boolean isStarred) { this.isStarred = isStarred; return this; }
            public EmailSummaryDtoBuilder aiSummary(String aiSummary) { this.aiSummary = aiSummary; return this; }
            public EmailSummaryDtoBuilder aiCategory(String aiCategory) { this.aiCategory = aiCategory; return this; }
            public EmailSummaryDtoBuilder threadMessageCount(int threadMessageCount) { this.threadMessageCount = threadMessageCount; return this; }
            public EmailSummaryDto build() { return new EmailSummaryDto(id, gmailMessageId, gmailThreadId, senderEmail, senderName, subject, snippet, receivedAt, isRead, isStarred, aiSummary, aiCategory, threadMessageCount); }
        }
        public static EmailSummaryDtoBuilder builder() { return new EmailSummaryDtoBuilder(); }
    }

    public static class EmailDetailDto {
        private UUID id;
        private String gmailMessageId;
        private String gmailThreadId;
        private String senderEmail;
        private String senderName;
        private String[] recipientEmails;
        private String[] ccEmails;
        private String subject;
        private String snippet;
        private String bodyText;
        private String bodyHtml;
        private Instant receivedAt;
        private boolean isRead;
        private boolean isStarred;
        private boolean hasAttachments;
        private String[] gmailLabelIds;
        private String aiSummary;
        private String aiCategory;

        public EmailDetailDto() {}
        public EmailDetailDto(UUID id, String gmailMessageId, String gmailThreadId, String senderEmail, String senderName,
                              String[] recipientEmails, String[] ccEmails, String subject, String snippet, String bodyText,
                              String bodyHtml, Instant receivedAt, boolean isRead, boolean isStarred, boolean hasAttachments,
                              String[] gmailLabelIds, String aiSummary, String aiCategory) {
            this.id = id;
            this.gmailMessageId = gmailMessageId;
            this.gmailThreadId = gmailThreadId;
            this.senderEmail = senderEmail;
            this.senderName = senderName;
            this.recipientEmails = recipientEmails;
            this.ccEmails = ccEmails;
            this.subject = subject;
            this.snippet = snippet;
            this.bodyText = bodyText;
            this.bodyHtml = bodyHtml;
            this.receivedAt = receivedAt;
            this.isRead = isRead;
            this.isStarred = isStarred;
            this.hasAttachments = hasAttachments;
            this.gmailLabelIds = gmailLabelIds;
            this.aiSummary = aiSummary;
            this.aiCategory = aiCategory;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
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
        public boolean getIsRead() { return isRead; }
        public void setIsRead(boolean isRead) { this.isRead = isRead; }
        public boolean getIsStarred() { return isStarred; }
        public void setIsStarred(boolean isStarred) { this.isStarred = isStarred; }
        public boolean getIsHasAttachments() { return hasAttachments; }
        public void setHasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; }
        public String[] getGmailLabelIds() { return gmailLabelIds; }
        public void setGmailLabelIds(String[] gmailLabelIds) { this.gmailLabelIds = gmailLabelIds; }
        public String getAiSummary() { return aiSummary; }
        public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
        public String getAiCategory() { return aiCategory; }
        public void setAiCategory(String aiCategory) { this.aiCategory = aiCategory; }

        public static class EmailDetailDtoBuilder {
            private UUID id;
            private String gmailMessageId;
            private String gmailThreadId;
            private String senderEmail;
            private String senderName;
            private String[] recipientEmails;
            private String[] ccEmails;
            private String subject;
            private String snippet;
            private String bodyText;
            private String bodyHtml;
            private Instant receivedAt;
            private boolean isRead;
            private boolean isStarred;
            private boolean hasAttachments;
            private String[] gmailLabelIds;
            private String aiSummary;
            private String aiCategory;

            public EmailDetailDtoBuilder id(UUID id) { this.id = id; return this; }
            public EmailDetailDtoBuilder gmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; return this; }
            public EmailDetailDtoBuilder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
            public EmailDetailDtoBuilder senderEmail(String senderEmail) { this.senderEmail = senderEmail; return this; }
            public EmailDetailDtoBuilder senderName(String senderName) { this.senderName = senderName; return this; }
            public EmailDetailDtoBuilder recipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; return this; }
            public EmailDetailDtoBuilder ccEmails(String[] ccEmails) { this.ccEmails = ccEmails; return this; }
            public EmailDetailDtoBuilder subject(String subject) { this.subject = subject; return this; }
            public EmailDetailDtoBuilder snippet(String snippet) { this.snippet = snippet; return this; }
            public EmailDetailDtoBuilder bodyText(String bodyText) { this.bodyText = bodyText; return this; }
            public EmailDetailDtoBuilder bodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; return this; }
            public EmailDetailDtoBuilder receivedAt(Instant receivedAt) { this.receivedAt = receivedAt; return this; }
            public EmailDetailDtoBuilder isRead(boolean isRead) { this.isRead = isRead; return this; }
            public EmailDetailDtoBuilder isStarred(boolean isStarred) { this.isStarred = isStarred; return this; }
            public EmailDetailDtoBuilder hasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; return this; }
            public EmailDetailDtoBuilder gmailLabelIds(String[] gmailLabelIds) { this.gmailLabelIds = gmailLabelIds; return this; }
            public EmailDetailDtoBuilder aiSummary(String aiSummary) { this.aiSummary = aiSummary; return this; }
            public EmailDetailDtoBuilder aiCategory(String aiCategory) { this.aiCategory = aiCategory; return this; }
            public EmailDetailDto build() { return new EmailDetailDto(id, gmailMessageId, gmailThreadId, senderEmail, senderName, recipientEmails, ccEmails, subject, snippet, bodyText, bodyHtml, receivedAt, isRead, isStarred, hasAttachments, gmailLabelIds, aiSummary, aiCategory); }
        }
        public static EmailDetailDtoBuilder builder() { return new EmailDetailDtoBuilder(); }
    }

    // ─── Thread ─────────────────────────────────────────────
    public static class ThreadDetailDto {
        private UUID id;
        private String gmailThreadId;
        private String subject;
        private int messageCount;
        private Instant lastMessageAt;
        private String[] participants;
        private String aiSummary;
        private List<EmailDetailDto> messages;

        public ThreadDetailDto() {}
        public ThreadDetailDto(UUID id, String gmailThreadId, String subject, int messageCount, Instant lastMessageAt,
                               String[] participants, String aiSummary, List<EmailDetailDto> messages) {
            this.id = id;
            this.gmailThreadId = gmailThreadId;
            this.subject = subject;
            this.messageCount = messageCount;
            this.lastMessageAt = lastMessageAt;
            this.participants = participants;
            this.aiSummary = aiSummary;
            this.messages = messages;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getGmailThreadId() { return gmailThreadId; }
        public void setGmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
        public Instant getLastMessageAt() { return lastMessageAt; }
        public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }
        public String[] getParticipants() { return participants; }
        public void setParticipants(String[] participants) { this.participants = participants; }
        public String getAiSummary() { return aiSummary; }
        public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
        public List<EmailDetailDto> getMessages() { return messages; }
        public void setMessages(List<EmailDetailDto> messages) { this.messages = messages; }

        public static class ThreadDetailDtoBuilder {
            private UUID id;
            private String gmailThreadId;
            private String subject;
            private int messageCount;
            private Instant lastMessageAt;
            private String[] participants;
            private String aiSummary;
            private List<EmailDetailDto> messages;

            public ThreadDetailDtoBuilder id(UUID id) { this.id = id; return this; }
            public ThreadDetailDtoBuilder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
            public ThreadDetailDtoBuilder subject(String subject) { this.subject = subject; return this; }
            public ThreadDetailDtoBuilder messageCount(int messageCount) { this.messageCount = messageCount; return this; }
            public ThreadDetailDtoBuilder lastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; return this; }
            public ThreadDetailDtoBuilder participants(String[] participants) { this.participants = participants; return this; }
            public ThreadDetailDtoBuilder aiSummary(String aiSummary) { this.aiSummary = aiSummary; return this; }
            public ThreadDetailDtoBuilder messages(List<EmailDetailDto> messages) { this.messages = messages; return this; }
            public ThreadDetailDto build() { return new ThreadDetailDto(id, gmailThreadId, subject, messageCount, lastMessageAt, participants, aiSummary, messages); }
        }
        public static ThreadDetailDtoBuilder builder() { return new ThreadDetailDtoBuilder(); }
    }

    // ─── Sync ───────────────────────────────────────────────
    public static class SyncStatusDto {
        private String status;
        private int totalMessagesSynced;
        private Instant lastSyncAt;
        private String errorMessage;

        public SyncStatusDto() {}
        public SyncStatusDto(String status, int totalMessagesSynced, Instant lastSyncAt, String errorMessage) {
            this.status = status;
            this.totalMessagesSynced = totalMessagesSynced;
            this.lastSyncAt = lastSyncAt;
            this.errorMessage = errorMessage;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getTotalMessagesSynced() { return totalMessagesSynced; }
        public void setTotalMessagesSynced(int totalMessagesSynced) { this.totalMessagesSynced = totalMessagesSynced; }
        public Instant getLastSyncAt() { return lastSyncAt; }
        public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public static class SyncStatusDtoBuilder {
            private String status;
            private int totalMessagesSynced;
            private Instant lastSyncAt;
            private String errorMessage;

            public SyncStatusDtoBuilder status(String status) { this.status = status; return this; }
            public SyncStatusDtoBuilder totalMessagesSynced(int totalMessagesSynced) { this.totalMessagesSynced = totalMessagesSynced; return this; }
            public SyncStatusDtoBuilder lastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; return this; }
            public SyncStatusDtoBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
            public SyncStatusDto build() { return new SyncStatusDto(status, totalMessagesSynced, lastSyncAt, errorMessage); }
        }
        public static SyncStatusDtoBuilder builder() { return new SyncStatusDtoBuilder(); }
    }

    public static class SyncResponse {
        private String message;
        private SyncStatusDto syncStatus;

        public SyncResponse() {}
        public SyncResponse(String message, SyncStatusDto syncStatus) {
            this.message = message;
            this.syncStatus = syncStatus;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public SyncStatusDto getSyncStatus() { return syncStatus; }
        public void setSyncStatus(SyncStatusDto syncStatus) { this.syncStatus = syncStatus; }

        public static class SyncResponseBuilder {
            private String message;
            private SyncStatusDto syncStatus;
            public SyncResponseBuilder message(String message) { this.message = message; return this; }
            public SyncResponseBuilder syncStatus(SyncStatusDto syncStatus) { this.syncStatus = syncStatus; return this; }
            public SyncResponse build() { return new SyncResponse(message, syncStatus); }
        }
        public static SyncResponseBuilder builder() { return new SyncResponseBuilder(); }
    }

    // ─── AI ─────────────────────────────────────────────────
    public static class SummaryResponse {
        private UUID entityId;
        private String summary;
        private String modelUsed;

        public SummaryResponse() {}
        public SummaryResponse(UUID entityId, String summary, String modelUsed) {
            this.entityId = entityId;
            this.summary = summary;
            this.modelUsed = modelUsed;
        }

        public UUID getEntityId() { return entityId; }
        public void setEntityId(UUID entityId) { this.entityId = entityId; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getModelUsed() { return modelUsed; }
        public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

        public static class SummaryResponseBuilder {
            private UUID entityId;
            private String summary;
            private String modelUsed;
            public SummaryResponseBuilder entityId(UUID entityId) { this.entityId = entityId; return this; }
            public SummaryResponseBuilder summary(String summary) { this.summary = summary; return this; }
            public SummaryResponseBuilder modelUsed(String modelUsed) { this.modelUsed = modelUsed; return this; }
            public SummaryResponse build() { return new SummaryResponse(entityId, summary, modelUsed); }
        }
        public static SummaryResponseBuilder builder() { return new SummaryResponseBuilder(); }
    }

    public static class CategoryResponse {
        private UUID emailId;
        private String category;
        private float confidence;
        private String modelUsed;

        public CategoryResponse() {}
        public CategoryResponse(UUID emailId, String category, float confidence, String modelUsed) {
            this.emailId = emailId;
            this.category = category;
            this.confidence = confidence;
            this.modelUsed = modelUsed;
        }

        public UUID getEmailId() { return emailId; }
        public void setEmailId(UUID emailId) { this.emailId = emailId; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public float getConfidence() { return confidence; }
        public void setConfidence(float confidence) { this.confidence = confidence; }
        public String getModelUsed() { return modelUsed; }
        public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

        public static class CategoryResponseBuilder {
            private UUID emailId;
            private String category;
            private float confidence;
            private String modelUsed;
            public CategoryResponseBuilder emailId(UUID emailId) { this.emailId = emailId; return this; }
            public CategoryResponseBuilder category(String category) { this.category = category; return this; }
            public CategoryResponseBuilder confidence(float confidence) { this.confidence = confidence; return this; }
            public CategoryResponseBuilder modelUsed(String modelUsed) { this.modelUsed = modelUsed; return this; }
            public CategoryResponse build() { return new CategoryResponse(emailId, category, confidence, modelUsed); }
        }
        public static CategoryResponseBuilder builder() { return new CategoryResponseBuilder(); }
    }

    // ─── Chat ───────────────────────────────────────────────
    public static class ChatQueryRequest {
        private String question;
        private UUID sessionId;

        public ChatQueryRequest() {}
        public ChatQueryRequest(String question, UUID sessionId) {
            this.question = question;
            this.sessionId = sessionId;
        }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public UUID getSessionId() { return sessionId; }
        public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    }

    public static class ChatQueryResponse {
        private UUID sessionId;
        private String answer;
        private List<SourceCitation> sources;

        public ChatQueryResponse() {}
        public ChatQueryResponse(UUID sessionId, String answer, List<SourceCitation> sources) {
            this.sessionId = sessionId;
            this.answer = answer;
            this.sources = sources;
        }

        public UUID getSessionId() { return sessionId; }
        public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public List<SourceCitation> getSources() { return sources; }
        public void setSources(List<SourceCitation> sources) { this.sources = sources; }

        public static class ChatQueryResponseBuilder {
            private UUID sessionId;
            private String answer;
            private List<SourceCitation> sources;
            public ChatQueryResponseBuilder sessionId(UUID sessionId) { this.sessionId = sessionId; return this; }
            public ChatQueryResponseBuilder answer(String answer) { this.answer = answer; return this; }
            public ChatQueryResponseBuilder sources(List<SourceCitation> sources) { this.sources = sources; return this; }
            public ChatQueryResponse build() { return new ChatQueryResponse(sessionId, answer, sources); }
        }
        public static ChatQueryResponseBuilder builder() { return new ChatQueryResponseBuilder(); }
    }

    public static class SourceCitation {
        private UUID emailId;
        private String subject;
        private String senderEmail;
        private String senderName;
        private Instant date;
        private String gmailThreadId;
        private String relevantSnippet;

        public SourceCitation() {}
        public SourceCitation(UUID emailId, String subject, String senderEmail, String senderName, Instant date, String gmailThreadId, String relevantSnippet) {
            this.emailId = emailId;
            this.subject = subject;
            this.senderEmail = senderEmail;
            this.senderName = senderName;
            this.date = date;
            this.gmailThreadId = gmailThreadId;
            this.relevantSnippet = relevantSnippet;
        }

        public UUID getEmailId() { return emailId; }
        public void setEmailId(UUID emailId) { this.emailId = emailId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getSenderEmail() { return senderEmail; }
        public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public Instant getDate() { return date; }
        public void setDate(Instant date) { this.date = date; }
        public String getGmailThreadId() { return gmailThreadId; }
        public void setGmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; }
        public String getRelevantSnippet() { return relevantSnippet; }
        public void setRelevantSnippet(String relevantSnippet) { this.relevantSnippet = relevantSnippet; }

        public static class SourceCitationBuilder {
            private UUID emailId;
            private String subject;
            private String senderEmail;
            private String senderName;
            private Instant date;
            private String gmailThreadId;
            private String relevantSnippet;

            public SourceCitationBuilder emailId(UUID emailId) { this.emailId = emailId; return this; }
            public SourceCitationBuilder subject(String subject) { this.subject = subject; return this; }
            public SourceCitationBuilder senderEmail(String senderEmail) { this.senderEmail = senderEmail; return this; }
            public SourceCitationBuilder senderName(String senderName) { this.senderName = senderName; return this; }
            public SourceCitationBuilder date(Instant date) { this.date = date; return this; }
            public SourceCitationBuilder gmailThreadId(String gmailThreadId) { this.gmailThreadId = gmailThreadId; return this; }
            public SourceCitationBuilder relevantSnippet(String relevantSnippet) { this.relevantSnippet = relevantSnippet; return this; }
            public SourceCitation build() { return new SourceCitation(emailId, subject, senderEmail, senderName, date, gmailThreadId, relevantSnippet); }
        }
        public static SourceCitationBuilder builder() { return new SourceCitationBuilder(); }
    }

    // ─── Draft ──────────────────────────────────────────────
    public static class GenerateDraftRequest {
        private String prompt;
        private String[] recipientEmails;
        private String subject;

        public GenerateDraftRequest() {}
        public GenerateDraftRequest(String prompt, String[] recipientEmails, String subject) {
            this.prompt = prompt;
            this.recipientEmails = recipientEmails;
            this.subject = subject;
        }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String[] getRecipientEmails() { return recipientEmails; }
        public void setRecipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
    }

    public static class DraftResponse {
        private UUID draftId;
        private String subject;
        private String bodyText;
        private String[] recipientEmails;
        private String modelUsed;

        public DraftResponse() {}
        public DraftResponse(UUID draftId, String subject, String bodyText, String[] recipientEmails, String modelUsed) {
            this.draftId = draftId;
            this.subject = subject;
            this.bodyText = bodyText;
            this.recipientEmails = recipientEmails;
            this.modelUsed = modelUsed;
        }

        public UUID getDraftId() { return draftId; }
        public void setDraftId(UUID draftId) { this.draftId = draftId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBodyText() { return bodyText; }
        public void setBodyText(String bodyText) { this.bodyText = bodyText; }
        public String[] getRecipientEmails() { return recipientEmails; }
        public void setRecipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; }
        public String getModelUsed() { return modelUsed; }
        public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

        public static class DraftResponseBuilder {
            private UUID draftId;
            private String subject;
            private String bodyText;
            private String[] recipientEmails;
            private String modelUsed;

            public DraftResponseBuilder draftId(UUID draftId) { this.draftId = draftId; return this; }
            public DraftResponseBuilder subject(String subject) { this.subject = subject; return this; }
            public DraftResponseBuilder bodyText(String bodyText) { this.bodyText = bodyText; return this; }
            public DraftResponseBuilder recipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; return this; }
            public DraftResponseBuilder modelUsed(String modelUsed) { this.modelUsed = modelUsed; return this; }
            public DraftResponse build() { return new DraftResponse(draftId, subject, bodyText, recipientEmails, modelUsed); }
        }
        public static DraftResponseBuilder builder() { return new DraftResponseBuilder(); }
    }

    public static class SendDraftRequest {
        private UUID draftId;
        private String subject;
        private String bodyText;
        private String[] recipientEmails;
        private String[] ccEmails;

        public SendDraftRequest() {}
        public SendDraftRequest(UUID draftId, String subject, String bodyText, String[] recipientEmails, String[] ccEmails) {
            this.draftId = draftId;
            this.subject = subject;
            this.bodyText = bodyText;
            this.recipientEmails = recipientEmails;
            this.ccEmails = ccEmails;
        }

        public UUID getDraftId() { return draftId; }
        public void setDraftId(UUID draftId) { this.draftId = draftId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBodyText() { return bodyText; }
        public void setBodyText(String bodyText) { this.bodyText = bodyText; }
        public String[] getRecipientEmails() { return recipientEmails; }
        public void setRecipientEmails(String[] recipientEmails) { this.recipientEmails = recipientEmails; }
        public String[] getCcEmails() { return ccEmails; }
        public void setCcEmails(String[] ccEmails) { this.ccEmails = ccEmails; }
    }

    public static class SendResponse {
        private boolean success;
        private String gmailMessageId;
        private String message;

        public SendResponse() {}
        public SendResponse(boolean success, String gmailMessageId, String message) {
            this.success = success;
            this.gmailMessageId = gmailMessageId;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getGmailMessageId() { return gmailMessageId; }
        public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static class SendResponseBuilder {
            private boolean success;
            private String gmailMessageId;
            private String message;
            public SendResponseBuilder success(boolean success) { this.success = success; return this; }
            public SendResponseBuilder gmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; return this; }
            public SendResponseBuilder message(String message) { this.message = message; return this; }
            public SendResponse build() { return new SendResponse(success, gmailMessageId, message); }
        }
        public static SendResponseBuilder builder() { return new SendResponseBuilder(); }
    }

    // ─── Reply ──────────────────────────────────────────────
    public static class GenerateReplyRequest {
        private UUID emailId;
        private String threadId;
        private String instruction;

        public GenerateReplyRequest() {}
        public GenerateReplyRequest(UUID emailId, String threadId, String instruction) {
            this.emailId = emailId;
            this.threadId = threadId;
            this.instruction = instruction;
        }

        public UUID getEmailId() { return emailId; }
        public void setEmailId(UUID emailId) { this.emailId = emailId; }
        public String getThreadId() { return threadId; }
        public void setThreadId(String threadId) { this.threadId = threadId; }
        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }
    }

    public static class ReplyResponse {
        private UUID draftId;
        private String subject;
        private String bodyText;
        private String inReplyTo;
        private String modelUsed;

        public ReplyResponse() {}
        public ReplyResponse(UUID draftId, String subject, String bodyText, String inReplyTo, String modelUsed) {
            this.draftId = draftId;
            this.subject = subject;
            this.bodyText = bodyText;
            this.inReplyTo = inReplyTo;
            this.modelUsed = modelUsed;
        }

        public UUID getDraftId() { return draftId; }
        public void setDraftId(UUID draftId) { this.draftId = draftId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBodyText() { return bodyText; }
        public void setBodyText(String bodyText) { this.bodyText = bodyText; }
        public String getInReplyTo() { return inReplyTo; }
        public void setInReplyTo(String inReplyTo) { this.inReplyTo = inReplyTo; }
        public String getModelUsed() { return modelUsed; }
        public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

        public static class ReplyResponseBuilder {
            private UUID draftId;
            private String subject;
            private String bodyText;
            private String inReplyTo;
            private String modelUsed;

            public ReplyResponseBuilder draftId(UUID draftId) { this.draftId = draftId; return this; }
            public ReplyResponseBuilder subject(String subject) { this.subject = subject; return this; }
            public ReplyResponseBuilder bodyText(String bodyText) { this.bodyText = bodyText; return this; }
            public ReplyResponseBuilder inReplyTo(String inReplyTo) { this.inReplyTo = inReplyTo; return this; }
            public ReplyResponseBuilder modelUsed(String modelUsed) { this.modelUsed = modelUsed; return this; }
            public ReplyResponse build() { return new ReplyResponse(draftId, subject, bodyText, inReplyTo, modelUsed); }
        }
        public static ReplyResponseBuilder builder() { return new ReplyResponseBuilder(); }
    }

    // ─── Dashboard ──────────────────────────────────────────
    public static class DashboardDto {
        private UserDto user;
        private int totalEmails;
        private int unreadEmails;
        private int totalThreads;
        private SyncStatusDto syncStatus;
        private CategoryBreakdownDto categoryBreakdown;
        private List<EmailSummaryDto> recentEmails;
        private List<TopSenderDto> topSenders;
        private List<ActivityDayDto> activity;

        public DashboardDto() {}
        public DashboardDto(UserDto user, int totalEmails, int unreadEmails, int totalThreads, SyncStatusDto syncStatus, CategoryBreakdownDto categoryBreakdown, List<EmailSummaryDto> recentEmails, List<TopSenderDto> topSenders, List<ActivityDayDto> activity) {
            this.user = user;
            this.totalEmails = totalEmails;
            this.unreadEmails = unreadEmails;
            this.totalThreads = totalThreads;
            this.syncStatus = syncStatus;
            this.categoryBreakdown = categoryBreakdown;
            this.recentEmails = recentEmails;
            this.topSenders = topSenders;
            this.activity = activity;
        }

        public UserDto getUser() { return user; }
        public void setUser(UserDto user) { this.user = user; }
        public int getTotalEmails() { return totalEmails; }
        public void setTotalEmails(int totalEmails) { this.totalEmails = totalEmails; }
        public int getUnreadEmails() { return unreadEmails; }
        public void setUnreadEmails(int unreadEmails) { this.unreadEmails = unreadEmails; }
        public int getTotalThreads() { return totalThreads; }
        public void setTotalThreads(int totalThreads) { this.totalThreads = totalThreads; }
        public SyncStatusDto getSyncStatus() { return syncStatus; }
        public void setSyncStatus(SyncStatusDto syncStatus) { this.syncStatus = syncStatus; }
        public CategoryBreakdownDto getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(CategoryBreakdownDto categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
        public List<EmailSummaryDto> getRecentEmails() { return recentEmails; }
        public void setRecentEmails(List<EmailSummaryDto> recentEmails) { this.recentEmails = recentEmails; }
        public List<TopSenderDto> getTopSenders() { return topSenders; }
        public void setTopSenders(List<TopSenderDto> topSenders) { this.topSenders = topSenders; }
        public List<ActivityDayDto> getActivity() { return activity; }
        public void setActivity(List<ActivityDayDto> activity) { this.activity = activity; }

        public static class DashboardDtoBuilder {
            private UserDto user;
            private int totalEmails;
            private int unreadEmails;
            private int totalThreads;
            private SyncStatusDto syncStatus;
            private CategoryBreakdownDto categoryBreakdown;
            private List<EmailSummaryDto> recentEmails;
            private List<TopSenderDto> topSenders;
            private List<ActivityDayDto> activity;

            public DashboardDtoBuilder user(UserDto user) { this.user = user; return this; }
            public DashboardDtoBuilder totalEmails(int totalEmails) { this.totalEmails = totalEmails; return this; }
            public DashboardDtoBuilder unreadEmails(int unreadEmails) { this.unreadEmails = unreadEmails; return this; }
            public DashboardDtoBuilder totalThreads(int totalThreads) { this.totalThreads = totalThreads; return this; }
            public DashboardDtoBuilder syncStatus(SyncStatusDto syncStatus) { this.syncStatus = syncStatus; return this; }
            public DashboardDtoBuilder categoryBreakdown(CategoryBreakdownDto categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; return this; }
            public DashboardDtoBuilder recentEmails(List<EmailSummaryDto> recentEmails) { this.recentEmails = recentEmails; return this; }
            public DashboardDtoBuilder topSenders(List<TopSenderDto> topSenders) { this.topSenders = topSenders; return this; }
            public DashboardDtoBuilder activity(List<ActivityDayDto> activity) { this.activity = activity; return this; }
            public DashboardDto build() { return new DashboardDto(user, totalEmails, unreadEmails, totalThreads, syncStatus, categoryBreakdown, recentEmails, topSenders, activity); }
        }
        public static DashboardDtoBuilder builder() { return new DashboardDtoBuilder(); }
    }

    public static class ActivityDayDto {
        private String day;
        private int count;

        public ActivityDayDto() {}
        public ActivityDayDto(String day, int count) {
            this.day = day;
            this.count = count;
        }

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public static ActivityDayDtoBuilder builder() { return new ActivityDayDtoBuilder(); }

        public static class ActivityDayDtoBuilder {
            private String day;
            private int count;

            public ActivityDayDtoBuilder day(String day) { this.day = day; return this; }
            public ActivityDayDtoBuilder count(int count) { this.count = count; return this; }
            public ActivityDayDto build() { return new ActivityDayDto(day, count); }
        }
    }

    public static class TopSenderDto {
        private String name;
        private String email;
        private int count;

        public TopSenderDto() {}
        public TopSenderDto(String name, String email, int count) {
            this.name = name;
            this.email = email;
            this.count = count;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public static class TopSenderDtoBuilder {
            private String name;
            private String email;
            private int count;

            public TopSenderDtoBuilder name(String name) { this.name = name; return this; }
            public TopSenderDtoBuilder email(String email) { this.email = email; return this; }
            public TopSenderDtoBuilder count(int count) { this.count = count; return this; }
            public TopSenderDto build() { return new TopSenderDto(name, email, count); }
        }
        public static TopSenderDtoBuilder builder() { return new TopSenderDtoBuilder(); }
    }

    public static class CategoryBreakdownDto {
        private int newsletters;
        private int jobRecruitment;
        private int finance;
        private int notifications;
        private int personal;
        private int workProfessional;
        private int uncategorized;

        public CategoryBreakdownDto() {}
        public CategoryBreakdownDto(int newsletters, int jobRecruitment, int finance, int notifications, int personal, int workProfessional, int uncategorized) {
            this.newsletters = newsletters;
            this.jobRecruitment = jobRecruitment;
            this.finance = finance;
            this.notifications = notifications;
            this.personal = personal;
            this.workProfessional = workProfessional;
            this.uncategorized = uncategorized;
        }

        public int getNewsletters() { return newsletters; }
        public void setNewsletters(int newsletters) { this.newsletters = newsletters; }
        public int getJobRecruitment() { return jobRecruitment; }
        public void setJobRecruitment(int jobRecruitment) { this.jobRecruitment = jobRecruitment; }
        public int getFinance() { return finance; }
        public void setFinance(int finance) { this.finance = finance; }
        public int getNotifications() { return notifications; }
        public void setNotifications(int notifications) { this.notifications = notifications; }
        public int getPersonal() { return personal; }
        public void setPersonal(int personal) { this.personal = personal; }
        public int getWorkProfessional() { return workProfessional; }
        public void setWorkProfessional(int workProfessional) { this.workProfessional = workProfessional; }
        public int getUncategorized() { return uncategorized; }
        public void setUncategorized(int uncategorized) { this.uncategorized = uncategorized; }

        public static class CategoryBreakdownDtoBuilder {
            private int newsletters;
            private int jobRecruitment;
            private int finance;
            private int notifications;
            private int personal;
            private int workProfessional;
            private int uncategorized;

            public CategoryBreakdownDtoBuilder newsletters(int newsletters) { this.newsletters = newsletters; return this; }
            public CategoryBreakdownDtoBuilder jobRecruitment(int jobRecruitment) { this.jobRecruitment = jobRecruitment; return this; }
            public CategoryBreakdownDtoBuilder finance(int finance) { this.finance = finance; return this; }
            public CategoryBreakdownDtoBuilder notifications(int notifications) { this.notifications = notifications; return this; }
            public CategoryBreakdownDtoBuilder personal(int personal) { this.personal = personal; return this; }
            public CategoryBreakdownDtoBuilder workProfessional(int workProfessional) { this.workProfessional = workProfessional; return this; }
            public CategoryBreakdownDtoBuilder uncategorized(int uncategorized) { this.uncategorized = uncategorized; return this; }
            public CategoryBreakdownDto build() { return new CategoryBreakdownDto(newsletters, jobRecruitment, finance, notifications, personal, workProfessional, uncategorized); }
        }
        public static CategoryBreakdownDtoBuilder builder() { return new CategoryBreakdownDtoBuilder(); }
    }
}
