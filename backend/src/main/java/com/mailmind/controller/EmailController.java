package com.mailmind.controller;

import com.mailmind.dto.ApiDtos.*;
import com.mailmind.entity.*;
import com.mailmind.repository.*;
import com.mailmind.gmail.GmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class EmailController {

    private final EmailRepository emailRepo;
    private final ThreadRepository threadRepo;
    private final GmailAccountRepository gmailAccountRepo;
    private final GmailService gmailService;
    private final SyncStateRepository syncStateRepo;
    private final JdbcTemplate jdbc;

    public EmailController(EmailRepository er, ThreadRepository tr, GmailAccountRepository gar, GmailService gs, SyncStateRepository ssr, JdbcTemplate jdbc) {
        this.emailRepo = er; this.threadRepo = tr; this.gmailAccountRepo = gar; this.gmailService = gs; this.syncStateRepo = ssr; this.jdbc = jdbc;
    }

    @GetMapping("/emails")
    public ResponseEntity<EmailListResponse> listEmails(Authentication auth,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "true") boolean inboxOnly,
            @RequestParam(required = false) String mailbox,
            @RequestParam(required = false) String label) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();

        if ((mailbox != null && !mailbox.isBlank()) || (label != null && !label.isBlank())) {
            return ResponseEntity.ok(listByGmailLabel(acct.getId(), mailbox, label, page, size));
        }

        Page<EmailSummaryProjection> emails;
        
        if (inboxOnly) {
            if (category == null || category.isEmpty() || "All".equalsIgnoreCase(category)) {
                emails = emailRepo.findInboxEmails(acct.getId(), PageRequest.of(page, size));
            } else if ("Primary".equalsIgnoreCase(category)) {
                emails = emailRepo.findInboxPrimaryEmails(acct.getId(), PageRequest.of(page, size));
            } else if ("Promotions".equalsIgnoreCase(category)) {
                emails = emailRepo.findInboxPromotionsEmails(acct.getId(), PageRequest.of(page, size));
            } else if ("Social".equalsIgnoreCase(category)) {
                emails = emailRepo.findInboxSocialEmails(acct.getId(), PageRequest.of(page, size));
            } else if ("Updates".equalsIgnoreCase(category)) {
                emails = emailRepo.findInboxUpdatesEmails(acct.getId(), PageRequest.of(page, size));
            } else {
                emails = emailRepo.findInboxEmailsByCategory(acct.getId(), category, PageRequest.of(page, size));
            }
        } else {
            if (category == null || category.isEmpty() || "All".equalsIgnoreCase(category)) {
                emails = emailRepo.findProjectedByGmailAccountIdOrderByReceivedAtDesc(acct.getId(), PageRequest.of(page, size));
            } else {
                emails = emailRepo.findProjectedByGmailAccountIdAndAiCategoryOrderByReceivedAtDesc(acct.getId(), category, PageRequest.of(page, size));
            }
        }
        
        List<EmailSummaryDto> dtos = emails.getContent().stream().map(this::toSummaryDto).collect(Collectors.toList());
        return ResponseEntity.ok(EmailListResponse.builder().emails(dtos)
            .totalCount((int)emails.getTotalElements()).page(page).pageSize(size).build());
    }

    @GetMapping("/email-labels")
    public ResponseEntity<List<String>> listLabels(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        List<String[]> labelArrays = jdbc.query("SELECT gmail_label_ids FROM emails WHERE gmail_account_id = ? AND gmail_label_ids IS NOT NULL", (rs, rowNum) -> (String[]) rs.getArray("gmail_label_ids").getArray(), acct.getId());
        Set<String> labels = new TreeSet<>();
        Set<String> hidden = Set.of("INBOX", "SENT", "DRAFT", "TRASH", "SPAM", "UNREAD", "STARRED", "IMPORTANT", "CATEGORY_PERSONAL", "CATEGORY_SOCIAL", "CATEGORY_PROMOTIONS", "CATEGORY_UPDATES", "CATEGORY_FORUMS");
        for (String[] arr : labelArrays) {
            if (arr != null) {
                for (String item : arr) {
                    if (item != null && !hidden.contains(item)) labels.add(item);
                }
            }
        }
        return ResponseEntity.ok(new ArrayList<>(labels));
    }

    private EmailListResponse listByGmailLabel(UUID accountId, String mailbox, String label, int page, int size) {
        String gmailLabel = label != null && !label.isBlank() ? label : mailboxToGmailLabel(mailbox);
        if (gmailLabel == null) gmailLabel = "INBOX";

        GmailAccount acct = gmailAccountRepo.findById(accountId).orElseThrow();
        String ownEmail = acct.getGmailEmail();
        boolean filterSent = !"SENT".equalsIgnoreCase(gmailLabel) && !"DRAFT".equalsIgnoreCase(gmailLabel);

        String countSql;
        List<Map<String, Object>> rows;
        if (filterSent && ownEmail != null) {
            countSql = "SELECT COUNT(*) FROM emails WHERE gmail_account_id = ? AND ? = ANY(gmail_label_ids) AND (sender_email IS NULL OR sender_email <> ?)";
            Integer total = jdbc.queryForObject(countSql, Integer.class, accountId, gmailLabel, ownEmail);
            rows = jdbc.queryForList(
                "SELECT e.id, e.gmail_message_id, e.gmail_thread_id, e.sender_email, e.sender_name, e.subject, e.snippet, e.received_at, e.is_read, e.is_starred, e.ai_summary, e.ai_category, COALESCE(t.message_count, 1) AS thread_message_count "
                + "FROM emails e LEFT JOIN threads t ON e.thread_id = t.id "
                + "WHERE e.gmail_account_id = ? AND ? = ANY(e.gmail_label_ids) AND (e.sender_email IS NULL OR e.sender_email <> ?) "
                + "ORDER BY e.received_at DESC LIMIT ? OFFSET ?",
                accountId, gmailLabel, ownEmail, size, page * size);
            List<EmailSummaryDto> dtos = rows.stream().map(this::toSummaryDto).collect(Collectors.toList());
            return EmailListResponse.builder().emails(dtos).totalCount(total != null ? total : 0).page(page).pageSize(size).build();
        } else {
            countSql = "SELECT COUNT(*) FROM emails WHERE gmail_account_id = ? AND ? = ANY(gmail_label_ids)";
            Integer total = jdbc.queryForObject(countSql, Integer.class, accountId, gmailLabel);
            rows = jdbc.queryForList(
                "SELECT e.id, e.gmail_message_id, e.gmail_thread_id, e.sender_email, e.sender_name, e.subject, e.snippet, e.received_at, e.is_read, e.is_starred, e.ai_summary, e.ai_category, COALESCE(t.message_count, 1) AS thread_message_count "
                + "FROM emails e LEFT JOIN threads t ON e.thread_id = t.id "
                + "WHERE e.gmail_account_id = ? AND ? = ANY(e.gmail_label_ids) "
                + "ORDER BY e.received_at DESC LIMIT ? OFFSET ?",
                accountId, gmailLabel, size, page * size);
            List<EmailSummaryDto> dtos = rows.stream().map(this::toSummaryDto).collect(Collectors.toList());
            return EmailListResponse.builder().emails(dtos).totalCount(total != null ? total : 0).page(page).pageSize(size).build();
        }
    }

    private String mailboxToGmailLabel(String mailbox) {
        if (mailbox == null) return null;
        return switch (mailbox.toLowerCase(Locale.ROOT)) {
            case "sent" -> "SENT";
            case "drafts", "draft" -> "DRAFT";
            case "trash" -> "TRASH";
            case "starred" -> "STARRED";
            case "snoozed" -> "SNOOZED";
            default -> mailbox.toUpperCase(Locale.ROOT);
        };
    }

    @GetMapping("/emails/{id}")
    public ResponseEntity<EmailDetailDto> getEmail(@PathVariable UUID id) {
        Email email = emailRepo.findById(id).orElseThrow();
        email = gmailService.enrichEmailIfNeeded(email);
        return ResponseEntity.ok(toDetailDto(email));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<ThreadDetailDto> getThread(@PathVariable String threadId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        EmailThread thread = threadRepo.findByGmailAccountIdAndGmailThreadId(acct.getId(), threadId).orElseThrow();
        List<Email> msgs = emailRepo.findByGmailAccountIdAndGmailThreadIdOrderByReceivedAtAsc(acct.getId(), threadId);
        return ResponseEntity.ok(ThreadDetailDto.builder().id(thread.getId()).gmailThreadId(thread.getGmailThreadId())
            .subject(thread.getSubject()).messageCount(thread.getMessageCount()).lastMessageAt(thread.getLastMessageAt())
            .participants(thread.getParticipants()).aiSummary(thread.getAiSummary())
            .messages(msgs.stream().map(this::toDetailDto).collect(Collectors.toList())).build());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> dashboard(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElse(null);
        User user = acct != null ? acct.getUser() : null;
        if (acct == null) return ResponseEntity.ok(DashboardDto.builder()
            .user(UserDto.builder().id(userId).gmailConnection(GmailConnectionDto.builder().connected(false).build()).build())
            .build());

        // Fetch and save Google profile info dynamically if needed
        user = gmailService.fetchAndSaveGoogleUserInfo(acct, user);

        // Fetch storage quota details from Google
        Map<String, Long> storage = gmailService.getStorageQuota(acct);
        Long limit = storage.get("limit");
        Long usage = storage.get("usage");

        int total = emailRepo.countReceivedEmails(acct.getId());
        int unread = emailRepo.countUnreadInboxReceivedEmails(acct.getId());
        int threads = threadRepo.countByGmailAccountId(acct.getId());
        Page<EmailSummaryProjection> recent = emailRepo.findRecentReceivedEmails(acct.getId(), PageRequest.of(0, 5));
        // Category breakdown
        List<Object[]> cats = emailRepo.countByCategory(acct.getId());
        CategoryBreakdownDto cbd = CategoryBreakdownDto.builder().build();
        for (Object[] c : cats) {
            String cat = (String) c[0]; long cnt = (Long) c[1];
            if (cat == null) cbd.setUncategorized((int)cnt);
            else switch(cat) {
                case "Newsletters": cbd.setNewsletters((int)cnt); break;
                case "Job/Recruitment": cbd.setJobRecruitment((int)cnt); break;
                case "Finance": cbd.setFinance((int)cnt); break;
                case "Notifications": cbd.setNotifications((int)cnt); break;
                case "Personal": cbd.setPersonal((int)cnt); break;
                case "Work/Professional": cbd.setWorkProfessional((int)cnt); break;
                default: cbd.setUncategorized(cbd.getUncategorized() + (int)cnt);
            }
        }
        
        List<Object[]> topSendersData = emailRepo.findTopSenders(acct.getId(), PageRequest.of(0, 5));
        List<TopSenderDto> topSenders = new ArrayList<>();
        for (Object[] row : topSendersData) {
            String email = (String) row[0];
            String name = (String) row[1];
            long count = (Long) row[2];
            
            String displayName = name;
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = email;
            }
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "Unknown";
            }
            
            topSenders.add(TopSenderDto.builder()
                .name(displayName)
                .email(email != null ? email : "")
                .count((int)count)
                .build());
        }

        // Calculate activity counts for last 7 days
        LocalDate today = LocalDate.now();
        Instant since = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<Instant> receivedTimes = emailRepo.findReceivedTimes(acct.getId(), since);

        Map<LocalDate, Integer> countsByDate = new HashMap<>();
        if (receivedTimes != null) {
            for (Instant time : receivedTimes) {
                if (time != null) {
                    LocalDate date = time.atZone(ZoneId.systemDefault()).toLocalDate();
                    countsByDate.put(date, countsByDate.getOrDefault(date, 0) + 1);
                }
            }
        }

        List<ActivityDayDto> activity = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E", Locale.ENGLISH);
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dayName = date.format(dayFormatter); // e.g. "Mon"
            int count = countsByDate.getOrDefault(date, 0);
            activity.add(ActivityDayDto.builder().day(dayName).count(count).build());
        }

        SyncState ss = syncStateRepo.findByGmailAccountId(acct.getId()).orElse(null);
        String syncStatus = ss != null ? ss.getSyncStatus() : "NEVER_SYNCED";
        SyncStatusDto syncStatusDto = ss != null ? SyncStatusDto.builder().status(ss.getSyncStatus())
            .totalMessagesSynced(ss.getTotalMessagesSynced()).lastSyncAt(ss.getLastSyncCompletedAt())
            .errorMessage(ss.getErrorMessage()).build() : null;

        return ResponseEntity.ok(DashboardDto.builder().totalEmails(total).unreadEmails(unread).totalThreads(threads)
            .categoryBreakdown(cbd).recentEmails(recent.getContent().stream().map(this::toSummaryDto).collect(Collectors.toList()))
            .topSenders(topSenders).activity(activity).syncStatus(syncStatusDto)
            .user(UserDto.builder().id(userId).email(user != null ? user.getEmail() : null)
                .displayName(user != null ? user.getDisplayName() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .gmailConnection(GmailConnectionDto.builder().connected(true).gmailEmail(acct.getGmailEmail())
                    .lastSyncAt(acct.getLastSyncAt()).totalEmailsSynced(total).syncStatus(syncStatus)
                    .storageLimit(limit).storageUsage(usage).build()).build())
            .build());
    }

    @PostMapping("/emails/{id}/star")
    public ResponseEntity<Void> toggleStar(@PathVariable UUID id, @RequestParam boolean starred) {
        gmailService.toggleStar(id, starred);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/emails/{id}/trash")
    public ResponseEntity<Void> trashEmail(@PathVariable UUID id) {
        gmailService.trashEmail(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/emails/{id}/snooze")
    public ResponseEntity<Void> snoozeEmail(@PathVariable UUID id) {
        gmailService.snoozeEmail(id);
        return ResponseEntity.ok().build();
    }


    private EmailSummaryDto toSummaryDto(EmailSummaryProjection e) {
        return EmailSummaryDto.builder().id(e.getId()).gmailMessageId(e.getGmailMessageId())
            .gmailThreadId(e.getGmailThreadId()).senderEmail(e.getSenderEmail()).senderName(e.getSenderName())
            .subject(e.getSubject()).snippet(e.getSnippet()).receivedAt(e.getReceivedAt())
            .isRead(e.getIsRead() != null && e.getIsRead()).isStarred(e.getIsStarred() != null && e.getIsStarred())
            .aiSummary(e.getAiSummary()).aiCategory(e.getAiCategory())
            .threadMessageCount(e.getThread() != null && e.getThread().getMessageCount() != null ? e.getThread().getMessageCount() : 1).build();
    }

    private EmailSummaryDto toSummaryDto(Map<String, Object> e) {
        Object receivedAt = e.get("received_at");
        Instant date = receivedAt instanceof Timestamp ts ? ts.toInstant() : (Instant) receivedAt;
        Number threadCount = (Number) e.get("thread_message_count");
        return EmailSummaryDto.builder().id((UUID) e.get("id")).gmailMessageId((String) e.get("gmail_message_id"))
            .gmailThreadId((String) e.get("gmail_thread_id")).senderEmail((String) e.get("sender_email")).senderName((String) e.get("sender_name"))
            .subject((String) e.get("subject")).snippet((String) e.get("snippet")).receivedAt(date)
            .isRead(Boolean.TRUE.equals(e.get("is_read"))).isStarred(Boolean.TRUE.equals(e.get("is_starred")))
            .aiSummary((String) e.get("ai_summary")).aiCategory((String) e.get("ai_category"))
            .threadMessageCount(threadCount != null ? threadCount.intValue() : 1).build();
    }

    private EmailDetailDto toDetailDto(Email e) {
        return EmailDetailDto.builder().id(e.getId()).gmailMessageId(e.getGmailMessageId())
            .gmailThreadId(e.getGmailThreadId()).senderEmail(e.getSenderEmail()).senderName(e.getSenderName())
            .recipientEmails(e.getRecipientEmails()).ccEmails(e.getCcEmails())
            .subject(e.getSubject()).snippet(e.getSnippet()).bodyText(e.getBodyText()).bodyHtml(e.getBodyHtml())
            .receivedAt(e.getReceivedAt()).isRead(e.getIsRead() != null && e.getIsRead())
            .isStarred(e.getIsStarred() != null && e.getIsStarred())
            .hasAttachments(e.getHasAttachments() != null && e.getHasAttachments())
            .gmailLabelIds(e.getGmailLabelIds()).aiSummary(e.getAiSummary()).aiCategory(e.getAiCategory()).build();
    }
}
