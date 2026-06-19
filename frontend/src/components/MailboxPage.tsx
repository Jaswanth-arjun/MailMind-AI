'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { EmailSummary } from '@/lib/types';
import { Star, Clock, Trash2 } from 'lucide-react';

type MailboxPageProps = {
  title: string;
  icon: string;
  mailbox?: string;
  label?: string;
  emptyText?: string;
};

function formatTime(iso: string) {
  const d = new Date(iso);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export default function MailboxPage({ title, icon, mailbox, label, emptyText }: MailboxPageProps) {
  const [emails, setEmails] = useState<EmailSummary[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    api.getEmails(0, 100, undefined, false, mailbox, label)
      .then(res => setEmails(res.emails))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [mailbox, label]);

  const filtered = emails.filter(e => {
    const query = search.toLowerCase();
    return !query
      || e.subject?.toLowerCase().includes(query)
      || e.senderName?.toLowerCase().includes(query)
      || e.senderEmail?.toLowerCase().includes(query);
  });

  const handleToggleStar = async (e: React.MouseEvent, id: string, currentlyStarred: boolean) => {
    e.preventDefault();
    e.stopPropagation();
    setEmails(prev => prev.map(email => email.id === id ? { ...email, isStarred: !currentlyStarred } : email));
    try {
      await api.toggleStar(id, !currentlyStarred);
    } catch (err) {
      console.error('Failed to toggle star:', err);
      setEmails(prev => prev.map(email => email.id === id ? { ...email, isStarred: currentlyStarred } : email));
    }
  };

  const handleTrash = async (e: React.MouseEvent, id: string) => {
    e.preventDefault();
    e.stopPropagation();
    const previousEmails = [...emails];
    setEmails(prev => prev.filter(email => email.id !== id));
    try {
      await api.trashEmail(id);
    } catch (err) {
      console.error('Failed to trash email:', err);
      setEmails(previousEmails);
    }
  };

  const handleSnooze = async (e: React.MouseEvent, id: string) => {
    e.preventDefault();
    e.stopPropagation();
    const previousEmails = [...emails];
    setEmails(prev => prev.filter(email => email.id !== id));
    try {
      await api.snoozeEmail(id);
    } catch (err) {
      console.error('Failed to snooze email:', err);
      setEmails(previousEmails);
    }
  };

  return (
    <div className="animate-fade-in max-w-5xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">{icon} {title}</h1>
        <p className="mt-1 text-sm text-text-muted">{emails.length} messages</p>
      </div>

      <div className="mb-6">
        <input
          type="text"
          placeholder={`Search ${title.toLowerCase()}...`}
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="input-field w-full"
        />
      </div>

      {loading ? (
        <div className="space-y-2">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="card flex items-start gap-4 !p-4 animate-pulse">
              <div className="h-11 w-11 shrink-0 rounded-full bg-surface-lighter/50" />
              <div className="min-w-0 flex-1 space-y-3">
                <div className="h-4 w-1/4 rounded bg-surface-lighter/50" />
                <div className="h-4 w-3/4 rounded bg-surface-lighter/50" />
                <div className="h-3 w-5/6 rounded bg-surface-lighter/50" />
              </div>
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="card !p-10 text-center text-text-muted">
          {emptyText || `No messages found in ${title}.`}
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map(email => (
            <Link
              key={email.id}
              href={`/dashboard/emails/${email.id}`}
              className={`card flex cursor-pointer items-start gap-4 !p-4 group relative ${!email.isRead ? 'border-l-2 border-l-primary' : ''}`}
            >
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary/30 to-accent/30 font-semibold">
                {email.senderName?.charAt(0) || email.senderEmail?.charAt(0) || '?'}
              </div>
              <div className="min-w-0 flex-1">
                <div className="mb-1 flex items-center justify-between">
                  <span className={`truncate font-medium ${!email.isRead ? 'text-text' : 'text-text-muted'}`}>
                    {email.senderName || email.senderEmail || 'Unknown sender'}
                  </span>
                  
                  {/* Time and Hover Actions */}
                  <div className="relative flex items-center justify-end shrink-0 ml-4 min-h-[28px]">
                    <span className="text-xs text-text-dim group-hover:hidden select-none">
                      {formatTime(email.receivedAt)}
                    </span>
                    <div className="hidden group-hover:flex items-center gap-1 bg-[#101422] border border-[#1a1f2e] rounded-lg p-0.5 shadow-lg backdrop-blur-sm">
                      <button
                        onClick={(e) => handleToggleStar(e, email.id, email.isStarred)}
                        className="p-1 hover:bg-[#1d243a] rounded text-[#8c9bb4] hover:text-[#eab308] transition-colors"
                        title={email.isStarred ? "Unstar" : "Star"}
                      >
                        <Star className={`w-3.5 h-3.5 ${email.isStarred ? 'fill-[#eab308] text-[#eab308]' : ''}`} />
                      </button>
                      <button
                        onClick={(e) => handleSnooze(e, email.id)}
                        className="p-1 hover:bg-[#1d243a] rounded text-[#8c9bb4] hover:text-[#f97316] transition-colors"
                        title="Snooze"
                      >
                        <Clock className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={(e) => handleTrash(e, email.id)}
                        className="p-1 hover:bg-[#1d243a] rounded text-[#8c9bb4] hover:text-[#ef4444] transition-colors"
                        title="Move to Trash"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                </div>
                <div className={`mb-1 truncate text-sm ${!email.isRead ? 'font-semibold' : 'text-text-muted'}`}>
                  {email.subject || '(No Subject)'}
                </div>
                <div className="mb-2 truncate text-xs text-text-dim">{email.aiSummary || email.snippet}</div>
                <div className="flex items-center gap-2">
                  {email.aiCategory && (
                    <span className={`category-badge border ${CATEGORY_COLORS[email.aiCategory] || CATEGORY_COLORS.Uncategorized}`}>
                      {CATEGORY_ICONS[email.aiCategory] || CATEGORY_ICONS.Uncategorized} {email.aiCategory}
                    </span>
                  )}
                  {email.isStarred && <Star className="w-3.5 h-3.5 fill-[#eab308] text-[#eab308]" />}
                  {email.threadMessageCount > 1 && <span className="text-xs text-text-dim">{email.threadMessageCount} messages</span>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
