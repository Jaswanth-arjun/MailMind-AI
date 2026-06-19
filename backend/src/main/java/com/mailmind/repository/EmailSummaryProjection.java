package com.mailmind.repository;

import java.time.Instant;
import java.util.UUID;

public interface EmailSummaryProjection {
    UUID getId();
    String getGmailMessageId();
    String getGmailThreadId();
    String getSenderEmail();
    String getSenderName();
    String getSubject();
    String getSnippet();
    Instant getReceivedAt();
    Boolean getIsRead();
    Boolean getIsStarred();
    String getAiSummary();
    String getAiCategory();
    ThreadSummaryProjection getThread();

    interface ThreadSummaryProjection {
        Integer getMessageCount();
    }
}
