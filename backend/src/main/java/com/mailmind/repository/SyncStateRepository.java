package com.mailmind.repository;

import com.mailmind.entity.SyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyncStateRepository extends JpaRepository<SyncState, UUID> {
    Optional<SyncState> findByGmailAccountId(UUID gmailAccountId);
}
