package com.mailmind.gmail;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.mailmind.entity.*;
import com.mailmind.repository.*;
import com.mailmind.ai.AiService;
import com.mailmind.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GmailService {
    private static final Logger log = LoggerFactory.getLogger(GmailService.class);
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.client-id}") private String clientId;
    @Value("${google.client-secret}") private String clientSecret;
    @Value("${google.redirect-uri}") private String redirectUri;
    @Value("${google.scopes}") private String scopes;
    @Value("${gmail.sync.max-results:100}") private int maxResults;
    @Value("${gmail.sync.rate-limit-retry-max:5}") private int maxRetries;
    @Value("${gmail.sync.rate-limit-base-delay-ms:1000}") private long baseDelayMs;
    @Value("${gmail.sync.initial-sync-days:30}") private int initialSyncDays;

    private final GmailAccountRepository gmailAccountRepo;
    private final EmailRepository emailRepo;
    private final ThreadRepository threadRepo;
    private final SyncStateRepository syncStateRepo;
    private final UserRepository userRepo;
    private final AiService aiService;
    private final RagService ragService;

    public GmailService(GmailAccountRepository gar, EmailRepository er, ThreadRepository tr,
                        SyncStateRepository ssr, UserRepository ur, AiService ai, RagService rag) {
        this.gmailAccountRepo = gar; this.emailRepo = er; this.threadRepo = tr;
        this.syncStateRepo = ssr; this.userRepo = ur; this.aiService = ai; this.ragService = rag;
    }

    public String getAuthorizationUrl() throws GeneralSecurityException, IOException {
        return buildFlow().newAuthorizationUrl().setRedirectUri(redirectUri)
            .setAccessType("offline").setApprovalPrompt("force").build();
    }

    public GmailAccount handleCallback(String code, UUID userId) throws GeneralSecurityException, IOException {
        GoogleTokenResponse tokenResponse = buildFlow().newTokenRequest(code).setRedirectUri(redirectUri).execute();
        Gmail gmail = buildGmailService(tokenResponse.getAccessToken());
        String gmailEmail = gmail.users().getProfile("me").execute().getEmailAddress();
        
        User user = null;
        if (userId != null) {
            user = userRepo.findById(userId).orElse(null);
        }
        if (user == null) {
            user = userRepo.findByEmail(gmailEmail).orElse(null);
        }
        if (user == null) {
            user = User.builder().email(gmailEmail).build();
        }
        
        // Fetch User Info from Google
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(tokenResponse.getAccessToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String name = (String) body.get("name");
                String picture = (String) body.get("picture");
                if (name != null) user.setDisplayName(name);
                if (picture != null) user.setAvatarUrl(picture);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Google user info: {}", e.getMessage());
        }
        
        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            user.setDisplayName(gmailEmail.split("@")[0]);
        }
        
        user = userRepo.save(user);
        
        final User finalUser = user;
        GmailAccount account = gmailAccountRepo.findByGmailEmail(gmailEmail)
            .orElseGet(() -> GmailAccount.builder().user(finalUser).gmailEmail(gmailEmail).connectedAt(Instant.now()).isActive(true).build());
        
        account.setUser(user);
        account.setAccessToken(tokenResponse.getAccessToken());
        account.setRefreshToken(tokenResponse.getRefreshToken());
        account.setTokenExpiresAt(Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
        account.setScopes(scopes);
        account.setIsActive(true);
        return gmailAccountRepo.save(account);
    }

    public User fetchAndSaveGoogleUserInfo(GmailAccount acct, User user) {
        if (acct == null || acct.getAccessToken() == null || user == null) return user;
        try {
            GmailAccount refreshed = refreshAccessToken(acct);
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(refreshed.getAccessToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String name = (String) body.get("name");
                String picture = (String) body.get("picture");
                boolean updated = false;
                if (name != null && !name.equals(user.getDisplayName())) {
                    user.setDisplayName(name);
                    updated = true;
                }
                if (picture != null && !picture.equals(user.getAvatarUrl())) {
                    user.setAvatarUrl(picture);
                    updated = true;
                }
                if (updated) {
                    user = userRepo.save(user);
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch userinfo from Google: {}", e.getMessage());
        }
        return user;
    }

    public Map<String, Long> getStorageQuota(GmailAccount acct) {
        Map<String, Long> quota = new HashMap<>();
        quota.put("limit", 16106127360L); // 15 GB default
        quota.put("usage", 13314398617L); // 12.4 GB default
        
        if (acct == null || acct.getAccessToken() == null) {
            return quota;
        }
        
        try {
            GmailAccount refreshed = refreshAccessToken(acct);
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(refreshed.getAccessToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/drive/v3/about?fields=storageQuota",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Map<String, Object> storageQuota = (Map<String, Object>) body.get("storageQuota");
                if (storageQuota != null) {
                    if (storageQuota.containsKey("limit")) {
                        quota.put("limit", Long.parseLong(storageQuota.get("limit").toString()));
                    }
                    if (storageQuota.containsKey("usage")) {
                        quota.put("usage", Long.parseLong(storageQuota.get("usage").toString()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch storage quota from Google Drive API: {}", e.getMessage());
        }
        return quota;
    }

    @Async("gmailSyncExecutor")
    public void syncEmails(GmailAccount accountParam) {
            GmailAccount account = gmailAccountRepo.findById(accountParam.getId()).orElse(accountParam);
            account = refreshAccessToken(account);
            SyncState ss = syncStateRepo.findByGmailAccountId(account.getId())
                .orElse(SyncState.builder().gmailAccount(account).syncStatus("IDLE").totalMessagesSynced(0).build());
            ss.setSyncStatus("IN_PROGRESS"); ss.setLastSyncStartedAt(Instant.now()); ss.setErrorMessage(null);
            syncStateRepo.save(ss);
            try {
                Gmail gmail = buildGmailService(account.getAccessToken());
            if (ss.getLastHistoryId() != null) performIncrementalSync(gmail, account, ss);
            else performInitialSync(gmail, account, ss);
            
            // AI Post-Sync processing
            processPendingEmails(account);

            ss.setSyncStatus("COMPLETED"); ss.setLastSyncCompletedAt(Instant.now());
            account.setLastSyncAt(Instant.now()); gmailAccountRepo.save(account);
        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage(), e);
            ss.setSyncStatus("FAILED"); ss.setErrorMessage(e.getMessage());
        }
        syncStateRepo.save(ss);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initInInboxField() {
        log.info("Initializing inInbox field for existing email records...");
        try {
            List<Email> emails = emailRepo.findByInInboxIsNull();
            if (!emails.isEmpty()) {
                log.info("Found {} emails with null inInbox status, updating...", emails.size());
                for (Email e : emails) {
                    boolean isInbox = false;
                    if (e.getGmailLabelIds() != null) {
                        isInbox = Arrays.asList(e.getGmailLabelIds()).contains("INBOX");
                    }
                    e.setInInbox(isInbox);
                    emailRepo.save(e);
                }
                log.info("Finished updating inInbox status.");
            } else {
                log.info("No email records with null inInbox status found.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize inInbox field: {}", e.getMessage());
        }
    }

    public void processPendingEmails(GmailAccount account) {
        log.info("Starting post-sync AI processing for account: {}", account.getGmailEmail());
        int totalProcessed = 0;
        int batchSize = 100;
        int maxBatches = 10;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Email> uncategorized = emailRepo.findUncategorized(account.getId(), org.springframework.data.domain.PageRequest.of(0, batchSize));
            if (uncategorized.isEmpty()) break;
            log.info("Processing {} newest uncategorized emails for account {}", uncategorized.size(), account.getGmailEmail());
            int processedThisBatch = 0;
            for (Email email : uncategorized) {
                try {
                    Map<String, Object> catResult = aiService.categorizeEmail(email.getBodyText(), email.getSubject(), email.getSenderEmail());
                    email.setAiCategory((String) catResult.get("category"));
                    email.setAiCategoryConfidence(((Number) catResult.get("confidence")).floatValue());
                    email.setAiCategorizedAt(Instant.now());

                    String summary = aiService.summarizeEmail(email.getBodyText(), email.getSubject(), email.getSenderEmail());
                    email.setAiSummary(summary);
                    email.setAiSummaryGeneratedAt(Instant.now());

                    emailRepo.save(email);
                    ragService.embedEmail(email);
                    processedThisBatch++;
                } catch (Exception e) {
                    log.error("Failed to process email {} with AI: {}", email.getId(), e.getMessage());
                }
            }
            totalProcessed += processedThisBatch;
            if (processedThisBatch == 0 || uncategorized.size() < batchSize) break;
        }
        int embedded = ragService.embedMissingEmails(account.getId(), 1000);
        log.info("Post-sync AI processing complete for {}. Processed: {}, auto-embedded missing: {}", account.getGmailEmail(), totalProcessed, embedded);
    }

    private void performInitialSync(Gmail gmail, GmailAccount account, SyncState ss) throws IOException, InterruptedException {
        String pageToken = ss.getPageToken();
        int synced = ss.getTotalMessagesSynced() != null ? ss.getTotalMessagesSynced() : 0;
        
        String q = "in:inbox OR in:sent OR in:drafts OR in:trash";
        if (initialSyncDays > 0) {
            LocalDate date = LocalDate.now().minusDays(initialSyncDays);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            q += " after:" + date.format(formatter);
            log.info("Performing initial sync for query: '{}' (limit days: {})", q, initialSyncDays);
        } else {
            log.info("Performing initial sync for query: '{}' (sync all messages)", q);
        }

        final String queryStr = q;
        do {
            String pt = pageToken;
            ListMessagesResponse resp = executeWithBackoff(() ->
                gmail.users().messages().list("me")
                    .setQ(queryStr)
                    .setMaxResults((long)maxResults)
                    .setPageToken(pt)
                    .execute());
            if (resp.getMessages() != null) {
                for (Message mr : resp.getMessages()) {
                    try {
                        Message fm = executeWithBackoff(() -> gmail.users().messages().get("me", mr.getId()).setFormat("full").execute());
                        saveEmail(fm, account); synced++;
                    } catch (Exception e) { log.warn("Skip msg {}: {}", mr.getId(), e.getMessage()); }
                }
            }
            pageToken = resp.getNextPageToken();
            ss.setPageToken(pageToken); ss.setTotalMessagesSynced(synced); syncStateRepo.save(ss);
        } while (pageToken != null);
        Profile profile = executeWithBackoff(() -> gmail.users().getProfile("me").execute());
        ss.setLastHistoryId(profile.getHistoryId().longValue()); ss.setPageToken(null);
    }

    private void performIncrementalSync(Gmail gmail, GmailAccount account, SyncState ss) throws IOException, InterruptedException {
        try {
            long hid = ss.getLastHistoryId();
            ListHistoryResponse hr = executeWithBackoff(() -> gmail.users().history().list("me").setStartHistoryId(java.math.BigInteger.valueOf(hid)).execute());
            if (hr.getHistory() != null) {
                Set<String> ids = new HashSet<>();
                for (History h : hr.getHistory()) if (h.getMessagesAdded() != null)
                    for (HistoryMessageAdded a : h.getMessagesAdded()) ids.add(a.getMessage().getId());
                int synced = ss.getTotalMessagesSynced() != null ? ss.getTotalMessagesSynced() : 0;
                for (String mid : ids) {
                    try {
                        Message fm = executeWithBackoff(() -> gmail.users().messages().get("me", mid).setFormat("full").execute());
                        saveEmail(fm, account); synced++;
                    } catch (Exception e) { log.warn("Skip msg {}: {}", mid, e.getMessage()); }
                }
                ss.setTotalMessagesSynced(synced);
            }
            if (hr.getHistoryId() != null) ss.setLastHistoryId(hr.getHistoryId().longValue());
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) { ss.setLastHistoryId(null); performInitialSync(gmail, account, ss); }
            else throw e;
        }
    }

    @Transactional
    public void saveEmail(Message message, GmailAccount account) {
        if (emailRepo.existsByGmailAccountIdAndGmailMessageId(account.getId(), message.getId())) return;
        
        List<String> labelIds = message.getLabelIds();
        if (labelIds != null) {
            if (labelIds.contains("SPAM")) {
                return;
            }
            boolean isCoreMailbox = labelIds.contains("INBOX") || labelIds.contains("SENT")
                || labelIds.contains("DRAFT") || labelIds.contains("TRASH")
                || labelIds.contains("STARRED") || labelIds.contains("SNOOZED");
            if (!isCoreMailbox) {
                return; // Skip archived messages
            }
        }

        Map<String, String> headers = new HashMap<>();
        if (message.getPayload() != null && message.getPayload().getHeaders() != null)
            for (MessagePartHeader h : message.getPayload().getHeaders()) headers.put(h.getName().toLowerCase(), h.getValue());
        
        StringBuilder textBuilder = new StringBuilder();
        StringBuilder htmlBuilder = new StringBuilder();
        extractBodyParts(message.getPayload(), textBuilder, htmlBuilder);
        
        String bodyText = textBuilder.toString();
        String bodyHtml = htmlBuilder.toString();
        
        // If bodyHtml is empty but bodyText looks like HTML, swap them/populate bodyHtml
        if ((bodyHtml == null || bodyHtml.isEmpty()) && bodyText != null && (bodyText.trim().startsWith("<") || bodyText.trim().toLowerCase().contains("<!doctype html"))) {
            bodyHtml = bodyText;
            bodyText = htmlToText(bodyHtml);
        }
        
        // Fallback for placeholder text in bodyText
        if (bodyText == null || bodyText.trim().isEmpty() || isHtmlPlaceholder(bodyText)) {
            if (bodyHtml != null && !bodyHtml.isEmpty()) {
                bodyText = htmlToText(bodyHtml);
            }
        }
        
        EmailThread thread = threadRepo.findByGmailAccountIdAndGmailThreadId(account.getId(), message.getThreadId())
            .orElseGet(() -> threadRepo.save(EmailThread.builder().gmailAccount(account).gmailThreadId(message.getThreadId())
                .subject(headers.getOrDefault("subject", "(No Subject)")).messageCount(0).build()));
        String from = headers.getOrDefault("from", "");
        
        String initialCategory = determineInitialCategory(message, headers, bodyText);
        boolean isInbox = labelIds != null && labelIds.contains("INBOX");
        boolean isDraft = labelIds != null && labelIds.contains("DRAFT");
        boolean isRead = labelIds != null && !labelIds.contains("UNREAD");
        boolean isStarred = labelIds != null && labelIds.contains("STARRED");
        String[] labelsArray = labelIds != null ? labelIds.toArray(new String[0]) : new String[0];

        Email email = Email.builder().gmailAccount(account).thread(thread).gmailMessageId(message.getId())
            .gmailThreadId(message.getThreadId()).senderEmail(extractEmail(from)).senderName(extractName(from))
            .recipientEmails(parseEmails(headers.getOrDefault("to", ""))).ccEmails(parseEmails(headers.getOrDefault("cc", "")))
            .subject(headers.getOrDefault("subject", "(No Subject)")).snippet(message.getSnippet())
            .bodyText(bodyText).bodyHtml(bodyHtml)
            .receivedAt(Instant.ofEpochMilli(message.getInternalDate())).internalDate(message.getInternalDate())
            .sizeEstimate(message.getSizeEstimate()).isRead(isRead).isStarred(isStarred)
            .isDraft(isDraft)
            .inInbox(isInbox)
            .gmailLabelIds(labelsArray).aiCategory(initialCategory).build();

        emailRepo.save(email);
        thread.setMessageCount(thread.getMessageCount() + 1); thread.setLastMessageAt(email.getReceivedAt()); threadRepo.save(thread);
    }

    private String determineInitialCategory(Message message, Map<String, String> headers, String bodyText) {
        List<String> labelIds = message.getLabelIds();
        String from = headers.getOrDefault("from", "").toLowerCase();
        String subject = headers.getOrDefault("subject", "").toLowerCase();
        
        // 1. Check for Newsletters (via List-Unsubscribe header or Promotions/Forums label)
        if (headers.containsKey("list-unsubscribe") || (labelIds != null && (labelIds.contains("CATEGORY_PROMOTIONS") || labelIds.contains("CATEGORY_FORUMS")))) {
            return "Newsletters";
        }
        
        // 2. Check for Finance
        if (subject.contains("invoice") || subject.contains("receipt") || subject.contains("billing") ||
            subject.contains("payment") || subject.contains("stripe") || subject.contains("paypal") ||
            subject.contains("statement") || subject.contains("bank") || subject.contains("transaction") ||
            from.contains("billing") || from.contains("payment") || from.contains("finance")) {
            return "Finance";
        }
        
        // 3. Check for Job/Recruitment
        if (subject.contains("job") || subject.contains("career") || subject.contains("hiring") ||
            subject.contains("interview") || subject.contains("resume") || subject.contains("application") ||
            subject.contains("naukri") || from.contains("naukri") || from.contains("recruit") ||
            from.contains("career") || from.contains("job")) {
            return "Job/Recruitment";
        }
        
        // 4. Check for Notifications (via Updates label or typical automated terms)
        if (labelIds != null && labelIds.contains("CATEGORY_UPDATES")) {
            return "Notifications";
        }
        if (subject.contains("notification") || subject.contains("alert") || subject.contains("update") ||
            subject.contains("verify") || subject.contains("security alert") || subject.contains("otp") ||
            from.contains("no-reply") || from.contains("noreply") || from.contains("notification")) {
            return "Notifications";
        }
        
        // 5. Check for Work/Professional (if from a corporate domain)
        String senderEmail = extractEmail(headers.getOrDefault("from", ""));
        if (!senderEmail.isEmpty() && senderEmail.contains("@")) {
            String domain = senderEmail.substring(senderEmail.indexOf("@") + 1).toLowerCase();
            List<String> personalDomains = Arrays.asList(
                "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "aol.com", "icloud.com", 
                "mail.com", "zoho.com", "protonmail.com", "gmx.com", "yandex.com", "live.com"
            );
            if (!personalDomains.contains(domain)) {
                return "Work/Professional";
            }
        }
        
        // 6. Check for Social or Personal labels
        if (labelIds != null && labelIds.contains("CATEGORY_SOCIAL")) {
            return "Social";
        }
        if (labelIds != null && labelIds.contains("CATEGORY_PERSONAL")) {
            return "Personal";
        }
        
        return "Personal"; // Default to Personal
    }

    public String sendEmail(GmailAccount acct, String to, String subj, String body, String replyTo, String threadId) throws Exception {
        GmailAccount account = gmailAccountRepo.findById(acct.getId()).orElse(acct);
        account = refreshAccessToken(account);
        Gmail gmail = buildGmailService(account.getAccessToken());
        StringBuilder sb = new StringBuilder();
        sb.append("From: ").append(account.getGmailEmail()).append("\r\n");
        sb.append("To: ").append(to).append("\r\n");
        sb.append("Subject: ").append(subj).append("\r\n");
        if (replyTo != null) { sb.append("In-Reply-To: ").append(replyTo).append("\r\n"); sb.append("References: ").append(replyTo).append("\r\n"); }
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n").append(body);
        Message msg = new Message(); msg.setRaw(Base64.getUrlEncoder().encodeToString(sb.toString().getBytes()));
        if (threadId != null) msg.setThreadId(threadId);
        return executeWithBackoff(() -> gmail.users().messages().send("me", msg).execute()).getId();
    }

    public Email enrichEmailIfNeeded(Email email) {
        if (email.getBodyHtml() != null && !email.getBodyHtml().isEmpty() && email.getBodyText() != null && !isHtmlPlaceholder(email.getBodyText())) {
            return email;
        }
        
        GmailAccount account = email.getGmailAccount();
        if (account == null || !account.getIsActive()) {
            return email;
        }
        
        try {
            account = refreshAccessToken(account);
            Gmail gmail = buildGmailService(account.getAccessToken());
            Message fm = executeWithBackoff(() -> gmail.users().messages().get("me", email.getGmailMessageId()).setFormat("full").execute());
            
            StringBuilder textBuilder = new StringBuilder();
            StringBuilder htmlBuilder = new StringBuilder();
            extractBodyParts(fm.getPayload(), textBuilder, htmlBuilder);
            
            String text = textBuilder.toString();
            String html = htmlBuilder.toString();
            
            // If html is empty but text looks like HTML, swap them/populate html
            if ((html == null || html.isEmpty()) && text != null && (text.trim().startsWith("<") || text.trim().toLowerCase().contains("<!doctype html"))) {
                html = text;
                text = htmlToText(html);
            }
            
            // Fallback for placeholder text in text
            if (text == null || text.trim().isEmpty() || isHtmlPlaceholder(text)) {
                if (html != null && !html.isEmpty()) {
                    text = htmlToText(html);
                }
            }
            
            email.setBodyText(text);
            email.setBodyHtml(html);
            
            // Re-summarize/re-categorize if the previous body text was empty/wrong
            if (email.getAiSummary() == null || email.getAiSummary().isEmpty() || email.getAiSummary().contains("No content") || email.getAiCategory() == null) {
                try {
                    Map<String, Object> catResult = aiService.categorizeEmail(text, email.getSubject(), email.getSenderEmail());
                    email.setAiCategory((String) catResult.get("category"));
                    email.setAiCategoryConfidence(((Number) catResult.get("confidence")).floatValue());
                    email.setAiCategorizedAt(Instant.now());
                    
                    String summary = aiService.summarizeEmail(text, email.getSubject(), email.getSenderEmail());
                    email.setAiSummary(summary);
                    email.setAiSummaryGeneratedAt(Instant.now());
                } catch (Exception ae) {
                    log.error("AI enrichment failed for email {}: {}", email.getId(), ae.getMessage());
                }
            }
            
            return emailRepo.save(email);
        } catch (Exception e) {
            log.error("Failed to enrich email on-the-fly: {}", e.getMessage());
            return email;
        }
    }

    public synchronized GmailAccount refreshAccessToken(GmailAccount account) {
        if (account.getTokenExpiresAt() != null && account.getTokenExpiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return account;
        }
        if (account.getRefreshToken() == null) {
            log.warn("No refresh token for account: {}", account.getGmailEmail());
            return account;
        }
        try {
            log.info("Refreshing access token for account: {}", account.getGmailEmail());
            GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                account.getRefreshToken(),
                clientId,
                clientSecret
            ).execute();
            account.setAccessToken(tokenResponse.getAccessToken());
            if (tokenResponse.getRefreshToken() != null) {
                account.setRefreshToken(tokenResponse.getRefreshToken());
            }
            account.setTokenExpiresAt(Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
            return gmailAccountRepo.save(account);
        } catch (Exception e) {
            log.error("Failed to refresh access token: {}", e.getMessage(), e);
            return account;
        }
    }

    public void toggleStar(UUID emailId, boolean star) {
        Email email = emailRepo.findById(emailId).orElseThrow();
        email.setIsStarred(star);
        List<String> labels = new ArrayList<>(Arrays.asList(email.getGmailLabelIds() != null ? email.getGmailLabelIds() : new String[0]));
        if (star) {
            if (!labels.contains("STARRED")) labels.add("STARRED");
        } else {
            labels.remove("STARRED");
        }
        email.setGmailLabelIds(labels.toArray(new String[0]));
        emailRepo.save(email);

        try {
            GmailAccount acct = email.getGmailAccount();
            if (acct != null && acct.getIsActive()) {
                GmailAccount refreshed = refreshAccessToken(acct);
                Gmail gmail = buildGmailService(refreshed.getAccessToken());
                ModifyMessageRequest modifyRequest = new ModifyMessageRequest();
                if (star) {
                    modifyRequest.setAddLabelIds(Collections.singletonList("STARRED"));
                } else {
                    modifyRequest.setRemoveLabelIds(Collections.singletonList("STARRED"));
                }
                gmail.users().messages().modify("me", email.getGmailMessageId(), modifyRequest).execute();
            }
        } catch (Exception e) {
            log.error("Failed to update star on Gmail API for message {}: {}", email.getGmailMessageId(), e.getMessage());
        }
    }

    public void trashEmail(UUID emailId) {
        Email email = emailRepo.findById(emailId).orElseThrow();
        email.setInInbox(false);
        List<String> labels = new ArrayList<>(Arrays.asList(email.getGmailLabelIds() != null ? email.getGmailLabelIds() : new String[0]));
        labels.remove("INBOX");
        if (!labels.contains("TRASH")) labels.add("TRASH");
        email.setGmailLabelIds(labels.toArray(new String[0]));
        emailRepo.save(email);

        try {
            GmailAccount acct = email.getGmailAccount();
            if (acct != null && acct.getIsActive()) {
                GmailAccount refreshed = refreshAccessToken(acct);
                Gmail gmail = buildGmailService(refreshed.getAccessToken());
                gmail.users().messages().trash("me", email.getGmailMessageId()).execute();
            }
        } catch (Exception e) {
            log.error("Failed to trash email on Gmail API for message {}: {}", email.getGmailMessageId(), e.getMessage());
        }
    }

    public void snoozeEmail(UUID emailId) {
        Email email = emailRepo.findById(emailId).orElseThrow();
        email.setInInbox(false);
        List<String> labels = new ArrayList<>(Arrays.asList(email.getGmailLabelIds() != null ? email.getGmailLabelIds() : new String[0]));
        labels.remove("INBOX");
        if (!labels.contains("SNOOZED")) labels.add("SNOOZED");
        email.setGmailLabelIds(labels.toArray(new String[0]));
        emailRepo.save(email);

        try {
            GmailAccount acct = email.getGmailAccount();
            if (acct != null && acct.getIsActive()) {
                GmailAccount refreshed = refreshAccessToken(acct);
                Gmail gmail = buildGmailService(refreshed.getAccessToken());
                ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                    .setAddLabelIds(Collections.singletonList("SNOOZED"))
                    .setRemoveLabelIds(Collections.singletonList("INBOX"));
                gmail.users().messages().modify("me", email.getGmailMessageId(), modifyRequest).execute();
            }
        } catch (Exception e) {
            log.error("Failed to snooze email on Gmail API for message {}: {}", email.getGmailMessageId(), e.getMessage());
        }
    }


    private void extractBodyParts(MessagePart part, StringBuilder textBuilder, StringBuilder htmlBuilder) {
        if (part == null) return;
        
        String mimeType = part.getMimeType();
        String data = (part.getBody() != null) ? part.getBody().getData() : null;
        
        if (data != null && !data.isEmpty()) {
            if ("text/plain".equalsIgnoreCase(mimeType)) {
                try {
                    String decoded = new String(Base64.getUrlDecoder().decode(data));
                    textBuilder.append(decoded);
                } catch (Exception e) {
                    log.warn("Failed to decode text part: {}", e.getMessage());
                }
            } else if ("text/html".equalsIgnoreCase(mimeType)) {
                try {
                    String decoded = new String(Base64.getUrlDecoder().decode(data));
                    htmlBuilder.append(decoded);
                } catch (Exception e) {
                    log.warn("Failed to decode html part: {}", e.getMessage());
                }
            }
        }
        
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                extractBodyParts(subPart, textBuilder, htmlBuilder);
            }
        }
    }

    private String htmlToText(String html) {
        if (html == null || html.isEmpty()) return "";
        String text = html.replaceAll("(?s)<style\\b[^>]*>.*?</style>", "");
        text = text.replaceAll("(?s)<script\\b[^>]*>.*?</script>", "");
        text = text.replaceAll("<[^>]*>", " ");
        text = text.replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'")
                   .replace("&nbsp;", " ");
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    private boolean isHtmlPlaceholder(String text) {
        if (text == null) return true;
        String lower = text.toLowerCase().trim();
        return lower.contains("enable html") || 
               lower.contains("enable javascript") || 
               lower.contains("view this email in a browser") || 
               lower.contains("click here to view") || 
               lower.length() < 3;
    }

    private String extractEmail(String h) { return h.contains("<") ? h.substring(h.indexOf("<")+1, h.indexOf(">")).trim() : h.trim(); }
    private String extractName(String h) { return h.contains("<") ? h.substring(0, h.indexOf("<")).trim().replace("\"","") : ""; }
    private String[] parseEmails(String h) { if (h == null || h.isEmpty()) return new String[0]; return Arrays.stream(h.split(",")).map(this::extractEmail).filter(e->!e.isEmpty()).toArray(String[]::new); }

    private Gmail buildGmailService(String token) throws GeneralSecurityException, IOException {
        NetHttpTransport t = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(t, JSON_FACTORY, new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(token)).setApplicationName("MailMind AI").build();
    }

    private GoogleAuthorizationCodeFlow buildFlow() throws GeneralSecurityException, IOException {
        return new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientId, clientSecret, Arrays.asList(scopes.split(","))).setAccessType("offline").build();
    }

    private <T> T executeWithBackoff(GmailApiCall<T> call) throws IOException, InterruptedException {
        int attempt = 0;
        while (true) {
            try { return call.execute(); }
            catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
                if (e.getStatusCode() == 429 && attempt < maxRetries) {
                    long delay = baseDelayMs * (long) Math.pow(2, attempt) + (long)(baseDelayMs * 0.1 * Math.random());
                    log.warn("Rate limit, retry in {}ms ({}/{})", delay, attempt+1, maxRetries); java.lang.Thread.sleep(delay); attempt++;
                } else throw e;
            }
        }
    }

    @FunctionalInterface interface GmailApiCall<T> { T execute() throws IOException; }
}
