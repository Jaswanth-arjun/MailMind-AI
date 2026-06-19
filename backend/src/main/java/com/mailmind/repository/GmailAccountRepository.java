package com.mailmind.repository;

import com.mailmind.entity.GmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GmailAccountRepository extends JpaRepository<GmailAccount, UUID> {
    Optional<GmailAccount> findByUserId(UUID userId);
    Optional<GmailAccount> findByUserIdAndIsActiveTrue(UUID userId);
    Optional<GmailAccount> findByGmailEmail(String gmailEmail);
}
