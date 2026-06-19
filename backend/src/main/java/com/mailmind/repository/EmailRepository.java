package com.mailmind.repository;

import com.mailmind.entity.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailRepository extends JpaRepository<Email, UUID> {

    Page<Email> findByGmailAccountIdOrderByReceivedAtDesc(UUID gmailAccountId, Pageable pageable);

    Page<EmailSummaryProjection> findProjectedByGmailAccountIdOrderByReceivedAtDesc(UUID gmailAccountId, Pageable pageable);

    List<Email> findByGmailAccountIdAndGmailThreadIdOrderByReceivedAtAsc(UUID gmailAccountId, String gmailThreadId);

    Optional<Email> findByGmailAccountIdAndGmailMessageId(UUID gmailAccountId, String gmailMessageId);

    boolean existsByGmailAccountIdAndGmailMessageId(UUID gmailAccountId, String gmailMessageId);

    int countByGmailAccountId(UUID gmailAccountId);

    int countByGmailAccountIdAndIsReadFalse(UUID gmailAccountId);

    Page<Email> findByGmailAccountIdAndAiCategoryOrderByReceivedAtDesc(
        UUID gmailAccountId, String aiCategory, Pageable pageable);

    Page<EmailSummaryProjection> findProjectedByGmailAccountIdAndAiCategoryOrderByReceivedAtDesc(
        UUID gmailAccountId, String aiCategory, Pageable pageable);

    @Query("SELECT e.aiCategory, COUNT(e) FROM Email e WHERE e.gmailAccount.id = :accountId GROUP BY e.aiCategory")
    List<Object[]> countByCategory(@Param("accountId") UUID accountId);

    @Query("SELECT e FROM Email e WHERE e.gmailAccount.id = :accountId AND e.aiSummary IS NULL ORDER BY e.receivedAt DESC")
    List<Email> findUnsummarized(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT e FROM Email e WHERE e.gmailAccount.id = :accountId AND e.aiCategorizedAt IS NULL ORDER BY e.receivedAt DESC")
    List<Email> findUncategorized(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT e.senderEmail, MAX(e.senderName), COUNT(e) as cnt FROM Email e WHERE e.gmailAccount.id = :accountId AND e.senderEmail IS NOT NULL GROUP BY e.senderEmail ORDER BY cnt DESC")
    List<Object[]> findTopSenders(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT e.receivedAt FROM Email e WHERE e.gmailAccount.id = :accountId AND e.receivedAt >= :since ORDER BY e.receivedAt ASC")
    List<Instant> findReceivedTimes(@Param("accountId") UUID accountId, @Param("since") Instant since);
}
