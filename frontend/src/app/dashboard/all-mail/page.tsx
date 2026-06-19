'use client';
import { useState, useEffect } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { EmailSummary } from '@/lib/types';
import { Star, Clock, Trash2 } from 'lucide-react';

const categories = ['All', 'Work/Professional', 'Personal', 'Finance', 'Newsletters', 'Job/Recruitment', 'Notifications'];

function formatTime(iso: string) {
  const d = new Date(iso);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  if (diff < 3600000) return `${Math.floor(diff/60000)}m ago`;
  if (diff < 86400000) return `${Math.floor(diff/3600000)}h ago`;
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export default function AllMailPage() {
  const [emails, setEmails] = useState<EmailSummary[]>([]);
  const [filter, setFilter] = useState('All');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  const fetchEmails = (currentFilter: string) => {
    setLoading(true);
    const catParam = currentFilter === 'All' ? undefined : currentFilter;
    api.getEmails(0, 100, catParam, false) // inboxOnly = false
      .then(res => setEmails(res.emails))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchEmails(filter);
  }, [filter]);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await api.triggerSync();
      let attempts = 0;
      const interval = setInterval(async () => {
        attempts++;
        try {
          const status = await api.getSyncStatus();
          if (status.status === 'COMPLETED' || status.status === 'FAILED' || attempts > 30) {
            clearInterval(interval);
            setSyncing(false);
            fetchEmails(filter);
          }
        } catch (e) {
          clearInterval(interval);
          setSyncing(false);
        }
      }, 2000);
    } catch (err) {
      console.error(err);
      setSyncing(false);
    }
  };

  const filtered = emails.filter(e => {
    if (search && !e.subject?.toLowerCase().includes(search.toLowerCase()) && !e.senderName?.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
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
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">📧 All Mail</h1>
        <button onClick={handleSync} disabled={syncing} className="btn-primary text-sm">
          {syncing ? '🔄 Syncing...' : '🔄 Sync'}
        </button>
      </div>

      {/* Search & Filter */}
      <div className="flex items-center gap-4 mb-6">
        <input type="text" placeholder="Search all emails..." value={search} onChange={e => setSearch(e.target.value)}
          className="input-field flex-1" />
      </div>
      <div className="flex gap-2 mb-6 flex-wrap">
        {categories.map(c => (
          <button key={c} onClick={() => setFilter(c)}
            className={`px-4 py-2 rounded-xl text-sm font-medium transition-all ${filter === c ? 'bg-primary text-white' : 'glass text-text-muted hover:text-text'}`}>
            {c === 'All' ? '📧' : CATEGORY_ICONS[c]} {c}
          </button>
        ))}
      </div>

      {/* Email List */}
      {loading ? (
        <div className="space-y-2">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="card flex items-start gap-4 !p-4 animate-pulse">
              <div className="w-11 h-11 rounded-full bg-surface-lighter/50 shrink-0" />
              <div className="flex-1 space-y-3 min-w-0">
                <div className="flex items-center justify-between">
                  <div className="h-4 bg-surface-lighter/50 rounded w-1/4" />
                  <div className="h-3 bg-surface-lighter/50 rounded w-16" />
                </div>
                <div className="h-4 bg-surface-lighter/50 rounded w-3/4" />
                <div className="h-3 bg-surface-lighter/50 rounded w-5/6" />
                <div className="flex gap-2">
                  <div className="h-5 bg-surface-lighter/50 rounded w-24" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 text-text-muted">
          <p className="text-4xl mb-3">📭</p>
          <p>No emails found matching your criteria</p>
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map((email) => (
            <Link key={email.id} href={`/dashboard/emails/${email.id}`}
              className={`card flex items-start gap-4 !p-4 cursor-pointer group relative ${!email.isRead ? 'border-l-2 border-l-primary' : ''}`}>
              <div className="w-11 h-11 rounded-full bg-gradient-to-br from-primary/30 to-accent/30 flex items-center justify-center font-semibold shrink-0">
                {email.senderName?.charAt(0) || '?'}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-1">
                  <span className={`font-medium ${!email.isRead ? 'text-text' : 'text-text-muted'}`}>{email.senderName}</span>
                  
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
                <div className={`text-sm mb-1 truncate ${!email.isRead ? 'font-semibold' : 'text-text-muted'}`}>{email.subject}</div>
                <div className="text-xs text-text-dim truncate mb-2">{email.aiSummary || email.snippet}</div>
                <div className="flex items-center gap-2">
                  {email.aiCategory && (
                    <span className={`category-badge border ${CATEGORY_COLORS[email.aiCategory] || CATEGORY_COLORS['Uncategorized']}`}>
                      {CATEGORY_ICONS[email.aiCategory]} {email.aiCategory}
                    </span>
                  )}
                  {email.isStarred && <Star className="w-3.5 h-3.5 fill-[#eab308] text-[#eab308]" />}
                  {email.threadMessageCount > 1 && <span className="text-xs text-text-dim">🧵 {email.threadMessageCount}</span>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
