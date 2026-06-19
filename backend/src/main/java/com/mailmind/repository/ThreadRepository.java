package com.mailmind.repository;

import com.mailmind.entity.EmailThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThreadRepository extends JpaRepository<EmailThread, UUID> {

    Optional<EmailThread> findByGmailAccountIdAndGmailThreadId(UUID gmailAccountId, String gmailThreadId);

    Page<EmailThread> findByGmailAccountIdOrderByLastMessageAtDesc(UUID gmailAccountId, Pageable pageable);

    int countByGmailAccountId(UUID gmailAccountId);

    boolean existsByGmailAccountIdAndGmailThreadId(UUID gmailAccountId, String gmailThreadId);
}
