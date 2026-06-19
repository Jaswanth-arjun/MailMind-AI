// MailMind AI - TypeScript Types

export interface User {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  gmailConnection: GmailConnection;
}

export interface GmailConnection {
  connected: boolean;
  gmailEmail?: string;
  lastSyncAt?: string;
  totalEmailsSynced: number;
  syncStatus?: string;
  storageLimit?: number;
  storageUsage?: number;
}

export interface EmailSummary {
  id: string;
  gmailMessageId: string;
  gmailThreadId: string;
  senderEmail: string;
  senderName: string;
  subject: string;
  snippet: string;
  receivedAt: string;
  isRead: boolean;
  isStarred: boolean;
  aiSummary?: string;
  aiCategory?: string;
  threadMessageCount: number;
}

export interface EmailDetail extends EmailSummary {
  recipientEmails?: string[];
  ccEmails?: string[];
  bodyText: string;
  bodyHtml?: string;
  hasAttachments: boolean;
  gmailLabelIds?: string[];
}

export interface ThreadDetail {
  id: string;
  gmailThreadId: string;
  subject: string;
  messageCount: number;
  lastMessageAt: string;
  participants?: string[];
  aiSummary?: string;
  messages: EmailDetail[];
}

export interface SyncStatus {
  status: string;
  totalMessagesSynced: number;
  lastSyncAt?: string;
  errorMessage?: string;
}

export interface ActivityDay {
  day: string;
  count: number;
}

export interface Dashboard {
  user: User;
  totalEmails: number;
  unreadEmails: number;
  totalThreads: number;
  syncStatus?: SyncStatus;
  categoryBreakdown: CategoryBreakdown;
  recentEmails: EmailSummary[];
  topSenders?: TopSender[];
  activity?: ActivityDay[];
}

export interface TopSender {
  name: string;
  email: string;
  count: number;
}

export interface CategoryBreakdown {
  newsletters: number;
  jobRecruitment: number;
  finance: number;
  notifications: number;
  personal: number;
  workProfessional: number;
  uncategorized: number;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  sources?: SourceCitation[];
  timestamp: string;
}

export interface SourceCitation {
  emailId: string;
  subject: string;
  senderEmail: string;
  senderName: string;
  date: string;
  gmailThreadId?: string;
}

export interface ChatResponse {
  sessionId: string;
  answer: string;
  sources: SourceCitation[];
}

export interface DraftResponse {
  draftId: string;
  subject: string;
  bodyText: string;
  recipientEmails?: string[];
  modelUsed: string;
}

export type EmailCategory = 
  | 'Newsletters'
  | 'Job/Recruitment' 
  | 'Finance'
  | 'Notifications'
  | 'Personal'
  | 'Work/Professional'
  | 'Uncategorized';

export const CATEGORY_COLORS: Record<string, string> = {
  'Newsletters': 'bg-purple-500/20 text-purple-300 border-purple-500/30',
  'Job/Recruitment': 'bg-blue-500/20 text-blue-300 border-blue-500/30',
  'Finance': 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
  'Notifications': 'bg-amber-500/20 text-amber-300 border-amber-500/30',
  'Personal': 'bg-pink-500/20 text-pink-300 border-pink-500/30',
  'Work/Professional': 'bg-cyan-500/20 text-cyan-300 border-cyan-500/30',
  'Social': 'bg-rose-500/20 text-rose-300 border-rose-500/30',
  'Primary': 'bg-pink-500/20 text-pink-300 border-pink-500/30',
  'Promotions': 'bg-purple-500/20 text-purple-300 border-purple-500/30',
  'Updates': 'bg-amber-500/20 text-amber-300 border-amber-500/30',
  'Uncategorized': 'bg-gray-500/20 text-gray-300 border-gray-500/30',
};

export const CATEGORY_ICONS: Record<string, string> = {
  'Newsletters': '📰',
  'Job/Recruitment': '💼',
  'Finance': '💰',
  'Notifications': '🔔',
  'Personal': '👤',
  'Work/Professional': '🏢',
  'Social': '👥',
  'Primary': '👤',
  'Promotions': '🏷️',
  'Updates': '🔔',
  'Uncategorized': '📧',
};
