'use client';
import { useState, useEffect } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { EmailSummary } from '@/lib/types';

const categories = ['Primary', 'Promotions', 'Social', 'Updates'];

function formatTime(iso: string) {
  const d = new Date(iso);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  if (diff < 3600000) return `${Math.floor(diff/60000)}m ago`;
  if (diff < 86400000) return `${Math.floor(diff/3600000)}h ago`;
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export default function EmailsPage() {
  const [emails, setEmails] = useState<EmailSummary[]>([]);
  const [filter, setFilter] = useState('Primary');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  const fetchEmails = (currentFilter: string) => {
    setLoading(true);
    api.getEmails(0, 50, currentFilter, true) // inboxOnly = true
      .then(res => setEmails(res.emails))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchEmails(filter);
  }, [filter]);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      const cat = params.get('category');
      if (cat && categories.includes(cat)) {
        setFilter(cat);
      }
    }
  }, []);

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

  return (
    <div className="animate-fade-in max-w-5xl">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">📬 Inbox</h1>
        <button onClick={handleSync} disabled={syncing} className="btn-primary text-sm">
          {syncing ? '🔄 Syncing...' : '🔄 Sync'}
        </button>
      </div>

      {/* Search & Filter */}
      <div className="flex items-center gap-4 mb-6">
        <input type="text" placeholder="Search inbox..." value={search} onChange={e => setSearch(e.target.value)}
          className="input-field flex-1" />
      </div>

      {/* Gmail Style Category Tabs */}
      <div className="flex border-b border-[#1a1f2e] mb-6 overflow-hidden rounded-xl bg-[#0b0f19]">
        {categories.map(c => {
          const active = filter === c;
          let activeStyle = '';
          switch(c) {
            case 'Primary':
              activeStyle = 'text-blue-400 border-blue-500 bg-blue-500/5';
              break;
            case 'Promotions':
              activeStyle = 'text-purple-400 border-purple-500 bg-purple-500/5';
              break;
            case 'Social':
              activeStyle = 'text-rose-400 border-rose-500 bg-rose-500/5';
              break;
            case 'Updates':
              activeStyle = 'text-amber-400 border-amber-500 bg-amber-500/5';
              break;
          }
          return (
            <button key={c} onClick={() => setFilter(c)}
              className={`flex-1 py-4 text-center text-sm font-semibold transition-all border-b-2 flex items-center justify-center gap-2
                ${active ? activeStyle : 'text-[#8c9bb4] border-transparent hover:text-white hover:bg-[#111520]'}`}>
              <span className="text-lg">{CATEGORY_ICONS[c]}</span>
              <span>{c}</span>
            </button>
          );
        })}
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
          <p>No emails found in this category</p>
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map((email) => (
            <Link key={email.id} href={`/dashboard/emails/${email.id}`}
              className={`card flex items-start gap-4 !p-4 cursor-pointer group ${!email.isRead ? 'border-l-2 border-l-primary' : ''}`}>
              <div className="w-11 h-11 rounded-full bg-gradient-to-br from-primary/30 to-accent/30 flex items-center justify-center font-semibold shrink-0">
                {email.senderName?.charAt(0) || '?'}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-1">
                  <span className={`font-medium ${!email.isRead ? 'text-text' : 'text-text-muted'}`}>{email.senderName}</span>
                  <span className="text-xs text-text-dim shrink-0 ml-4">{formatTime(email.receivedAt)}</span>
                </div>
                <div className={`text-sm mb-1 truncate ${!email.isRead ? 'font-semibold' : 'text-text-muted'}`}>{email.subject}</div>
                <div className="text-xs text-text-dim truncate mb-2">{email.aiSummary || email.snippet}</div>
                <div className="flex items-center gap-2">
                  {email.aiCategory && (
                    <span className={`category-badge border ${CATEGORY_COLORS[email.aiCategory] || CATEGORY_COLORS['Uncategorized']}`}>
                      {CATEGORY_ICONS[email.aiCategory]} {email.aiCategory}
                    </span>
                  )}
                  {email.isStarred && <span>⭐</span>}
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
