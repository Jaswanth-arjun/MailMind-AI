// MailMind AI - API Service Layer
import type { Dashboard, EmailSummary, EmailDetail, ThreadDetail, ChatResponse, DraftResponse } from './types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('mailmind_token');
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const prefix = '/api';
  const url = `${API_URL}${prefix}${path}`;
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers as Record<string, string> || {}),
  };
  const res = await fetch(url, { ...options, headers });
  if (!res.ok) throw new Error(`API Error ${res.status}: ${await res.text()}`);
  return res.json();
}

export const api = {
  // Auth
  getAuthUrl: () => apiFetch<{ authUrl: string }>('/gmail/auth-url'),

  // Dashboard
  getDashboard: () => apiFetch<Dashboard>('/dashboard'),

  // Emails
  getEmails: (page = 0, size = 20, category?: string) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (category) params.set('category', category);
    return apiFetch<{ emails: EmailSummary[]; totalCount: number }>(`/emails?${params}`);
  },

  getEmail: (id: string) => apiFetch<EmailDetail>(`/emails/${id}`),

  getThread: (threadId: string) => apiFetch<ThreadDetail>(`/threads/${threadId}`),

  // Sync
  triggerSync: () => apiFetch<{ message: string }>('/gmail/sync', { method: 'POST' }),
  getSyncStatus: () => apiFetch<{ status: string; totalMessagesSynced: number }>('/gmail/sync-status'),

  // AI
  summarizeEmail: (emailId: string) => apiFetch<{ summary: string }>(`/ai/summarize/email/${emailId}`, { method: 'POST' }),
  summarizeThread: (threadId: string) => apiFetch<{ summary: string }>(`/ai/summarize/thread/${threadId}`, { method: 'POST' }),
  categorize: (emailId: string) => apiFetch<{ category: string }>(`/ai/categorize/${emailId}`, { method: 'POST' }),

  // Chat
  chat: (question: string, sessionId?: string) =>
    apiFetch<ChatResponse>('/chat/query', { method: 'POST', body: JSON.stringify({ question, sessionId }) }),

  // Drafts
  generateDraft: (prompt: string, recipientEmails?: string[], subject?: string) =>
    apiFetch<DraftResponse>('/drafts/generate', { method: 'POST', body: JSON.stringify({ prompt, recipientEmails, subject }) }),

  sendDraft: (draftId: string, subject: string, bodyText: string, recipientEmails: string[]) =>
    apiFetch<{ success: boolean }>('/drafts/send', { method: 'POST', body: JSON.stringify({ draftId, subject, bodyText, recipientEmails }) }),

  // Reply
  generateReply: (emailId: string, threadId: string, instruction: string) =>
    apiFetch<DraftResponse>('/reply/generate', { method: 'POST', body: JSON.stringify({ emailId, threadId, instruction }) }),

  sendReply: (draftId: string, subject: string, bodyText: string, recipientEmails: string[]) =>
    apiFetch<{ success: boolean }>('/reply/send', { method: 'POST', body: JSON.stringify({ draftId, subject, bodyText, recipientEmails }) }),
};

