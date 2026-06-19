package com.mailmind.controller;

import com.mailmind.dto.ApiDtos.*;
import com.mailmind.entity.*;
import com.mailmind.repository.*;
import com.mailmind.gmail.GmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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

    public EmailController(EmailRepository er, ThreadRepository tr, GmailAccountRepository gar, GmailService gs, SyncStateRepository ssr) {
        this.emailRepo = er; this.threadRepo = tr; this.gmailAccountRepo = gar; this.gmailService = gs; this.syncStateRepo = ssr;
    }

    @GetMapping("/emails")
    public ResponseEntity<EmailListResponse> listEmails(Authentication auth,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        UUID userId = (UUID) auth.getPrincipal();
        GmailAccount acct = gmailAccountRepo.findByUserIdAndIsActiveTrue(userId).orElseThrow();
        Page<Email> emails;
        if (category != null && !category.isEmpty())
            emails = emailRepo.findByGmailAccountIdAndAiCategoryOrderByReceivedAtDesc(acct.getId(), category, PageRequest.of(page, size));
        else
            emails = emailRepo.findByGmailAccountIdOrderByReceivedAtDesc(acct.getId(), PageRequest.of(page, size));
        List<EmailSummaryDto> dtos = emails.getContent().stream().map(this::toSummaryDto).collect(Collectors.toList());
        return ResponseEntity.ok(EmailListResponse.builder().emails(dtos)
            .totalCount((int)emails.getTotalElements()).page(page).pageSize(size).build());
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

        int total = emailRepo.countByGmailAccountId(acct.getId());
        int unread = emailRepo.countByGmailAccountIdAndIsReadFalse(acct.getId());
        int threads = threadRepo.countByGmailAccountId(acct.getId());
        Page<Email> recent = emailRepo.findByGmailAccountIdOrderByReceivedAtDesc(acct.getId(), PageRequest.of(0, 5));
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
            String name = (String) row[0];
            long count = (Long) row[1];
            topSenders.add(TopSenderDto.builder().name(name != null ? name : "Unknown").email("").count((int)count).build());
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

    private EmailSummaryDto toSummaryDto(Email e) {
        return EmailSummaryDto.builder().id(e.getId()).gmailMessageId(e.getGmailMessageId())
            .gmailThreadId(e.getGmailThreadId()).senderEmail(e.getSenderEmail()).senderName(e.getSenderName())
            .subject(e.getSubject()).snippet(e.getSnippet()).receivedAt(e.getReceivedAt())
            .isRead(e.getIsRead() != null && e.getIsRead()).isStarred(e.getIsStarred() != null && e.getIsStarred())
            .aiSummary(e.getAiSummary()).aiCategory(e.getAiCategory())
            .threadMessageCount(e.getThread() != null ? e.getThread().getMessageCount() : 1).build();
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
