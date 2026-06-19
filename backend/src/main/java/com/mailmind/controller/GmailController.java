package com.mailmind.controller;

import com.mailmind.dto.ApiDtos.*;
import com.mailmind.entity.*;
import com.mailmind.gmail.GmailService;
import com.mailmind.repository.*;
import com.mailmind.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/gmail")
public class GmailController {

    private final GmailService gmailService;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepo;
    private final GmailAccountRepository gmailAccountRepo;
    private final SyncStateRepository syncStateRepo;
    @Value("${app.frontend-url}") private String frontendUrl;

    public GmailController(GmailService gs, JwtTokenProvider jp, UserRepository ur,
                            GmailAccountRepository gar, SyncStateRepository ssr) {
        this.gmailService = gs; this.jwtProvider = jp; this.userRepo = ur;
        this.gmailAccountRepo = gar; this.syncStateRepo = ssr;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<AuthUrlResponse> getAuthUrl() throws Exception {
        return ResponseEntity.ok(AuthUrlResponse.builder().authUrl(gmailService.getAuthorizationUrl()).build());
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code) throws Exception {
        // Create user if needed, exchange code for tokens
        GmailAccount account = gmailService.handleCallback(code, null);
        
        // Start sync immediately in background
        gmailService.syncEmails(account);
        
        String token = jwtProvider.generateToken(account.getUser().getId(), account.getUser().getEmail());
        // Redirect to frontend with token
        return ResponseEntity.status(302)
            .header("Location", frontendUrl + "/auth/callback?token=" + token)
            .build();
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> sync(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount account = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId)
            .orElseThrow(() -> new RuntimeException("No Gmail account connected"));
        gmailService.syncEmails(account);
        SyncState ss = syncStateRepo.findByGmailAccountId(account.getId()).orElse(null);
        return ResponseEntity.ok(SyncResponse.builder().message("Sync started")
            .syncStatus(ss != null ? SyncStatusDto.builder().status(ss.getSyncStatus())
                .totalMessagesSynced(ss.getTotalMessagesSynced()).lastSyncAt(ss.getLastSyncCompletedAt()).build() : null)
            .build());
    }

    @GetMapping("/sync-status")
    public ResponseEntity<SyncStatusDto> syncStatus(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount account = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId)
            .orElseThrow(() -> new RuntimeException("No Gmail account connected"));
        SyncState ss = syncStateRepo.findByGmailAccountId(account.getId())
            .orElse(SyncState.builder().syncStatus("NEVER_SYNCED").totalMessagesSynced(0).build());
        return ResponseEntity.ok(SyncStatusDto.builder().status(ss.getSyncStatus())
            .totalMessagesSynced(ss.getTotalMessagesSynced())
            .lastSyncAt(ss.getLastSyncCompletedAt())
            .errorMessage(ss.getErrorMessage()).build());
    }
}
