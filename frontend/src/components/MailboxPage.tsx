'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { EmailSummary } from '@/lib/types';
import { Star, Archive, Trash2 } from 'lucide-react';

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
  const router = useRouter();
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

  const handleArchive = async (e: React.MouseEvent, id: string) => {
    e.preventDefault();
    e.stopPropagation();
    const previousEmails = [...emails];
    setEmails(prev => prev.filter(email => email.id !== id));
    try {
      await api.archiveEmail(id);
    } catch (err) {
      console.error('Failed to archive email:', err);
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
            <div
              key={email.id}
              onClick={() => router.push(`/dashboard/emails/${email.id}`)}
              className={`card flex cursor-pointer items-start gap-4 !p-4 group relative transition-all duration-300 hover:scale-[1.008] hover:bg-[#111524] hover:shadow-[0_4px_25px_rgba(99,102,241,0.15)] hover:border-primary/40 ${!email.isRead ? 'border-l-2 border-l-primary' : ''}`}
            >
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary/30 to-accent/30 font-semibold select-none">
                {email.senderName?.charAt(0) || email.senderEmail?.charAt(0) || '?'}
              </div>
              <div className="min-w-0 flex-1 pr-24">
                <div className="mb-1 flex items-center justify-between">
                  <span className={`truncate font-medium ${!email.isRead ? 'text-text' : 'text-text-muted'}`}>
                    {email.senderName || email.senderEmail || 'Unknown sender'}
                  </span>
                  
                  {/* Time */}
                  <span className="text-xs text-text-dim group-hover:opacity-0 transition-opacity duration-200 select-none">
                    {formatTime(email.receivedAt)}
                  </span>
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
                  {email.isStarred && <Star className="w-3.5 h-3.5 fill-[#fbbf24] text-[#fbbf24]" />}
                  {email.threadMessageCount > 1 && <span className="text-xs text-text-dim">{email.threadMessageCount} messages</span>}
                </div>
              </div>

              {/* Hover Actions - Positioned at right side middle */}
              <div className="absolute right-4 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 flex items-center gap-1.5 bg-[#121626]/95 border border-[#2b354d] rounded-xl p-1.5 shadow-[0_4px_20px_rgba(0,0,0,0.4)] backdrop-blur-md z-10 transition-all duration-200 hover-menu-btn">
                <button
                  onClick={(e) => handleToggleStar(e, email.id, email.isStarred)}
                  className="p-2 hover:bg-[#1f2642] rounded-lg text-[#a3b3d4] hover:text-[#fbbf24] transition-all duration-200"
                  title={email.isStarred ? "Unstar" : "Star"}
                >
                  <Star className={`w-4 h-4 ${email.isStarred ? 'fill-[#fbbf24] text-[#fbbf24]' : ''}`} />
                </button>
                <button
                  onClick={(e) => handleArchive(e, email.id)}
                  className="p-2 hover:bg-[#1f2642] rounded-lg text-[#a3b3d4] hover:text-[#10b981] transition-all duration-200"
                  title="Archive"
                >
                  <Archive className="w-4 h-4" />
                </button>
                <button
                  onClick={(e) => handleTrash(e, email.id)}
                  className="p-2 hover:bg-[#1f2642] rounded-lg text-[#a3b3d4] hover:text-[#ef4444] transition-all duration-200"
                  title="Move to Trash"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
