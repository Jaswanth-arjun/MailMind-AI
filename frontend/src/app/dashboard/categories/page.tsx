'use client';
import { useState, useEffect } from 'react';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { Dashboard } from '@/lib/types';
import Link from 'next/link';

export default function CategoriesPage() {
  const [selected, setSelected] = useState('All');
  const [data, setData] = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getDashboard()
      .then(setData)
      .catch(console.error)
      .finally(() => setLoading(false));

    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      const cat = params.get('category');
      if (cat) {
        setSelected(cat);
      }
    }
  }, []);

  if (loading) return (
    <div className="animate-fade-in max-w-5xl">
      <div className="h-8 w-48 bg-surface-lighter/50 rounded animate-pulse mb-6" />

      <div className="grid grid-cols-3 gap-4 mb-8">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="card !p-4 space-y-3">
            <div className="flex items-center justify-between">
              <div className="w-8 h-8 rounded bg-surface-lighter/50 animate-pulse" />
              <div className="w-12 h-5 rounded bg-surface-lighter/50 animate-pulse" />
            </div>
            <div className="h-4 w-2/3 bg-surface-lighter/50 rounded animate-pulse" />
            <div className="h-3 w-1/3 bg-surface-lighter/50 rounded animate-pulse" />
            <div className="w-full h-1.5 bg-surface rounded-full overflow-hidden">
              <div className="h-full bg-surface-lighter/50 animate-pulse w-1/2" />
            </div>
          </div>
        ))}
      </div>

      <div className="h-5 w-36 bg-surface-lighter/50 rounded animate-pulse mb-4" />

      <div className="space-y-2">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="card !p-4 flex items-center gap-4">
            <div className="w-10 h-10 rounded-full bg-surface-lighter/50 animate-pulse shrink-0" />
            <div className="flex-1 space-y-2">
              <div className="h-4 w-1/3 bg-surface-lighter/50 rounded animate-pulse" />
              <div className="h-3 w-2/3 bg-surface-lighter/50 rounded animate-pulse" />
            </div>
            <div className="w-20 h-5 rounded bg-surface-lighter/50 animate-pulse shrink-0" />
          </div>
        ))}
      </div>
    </div>
  );

  if (!data) return null;

  const breakdown = data.categoryBreakdown;
  const total = Object.values(breakdown).reduce((a, b) => a + b, 0) || 1;

  const catStats = [
    { name: 'Work/Professional', count: breakdown.workProfessional, icon: '🏢', pct: Math.round((breakdown.workProfessional / total) * 100) },
    { name: 'Newsletters', count: breakdown.newsletters, icon: '📰', pct: Math.round((breakdown.newsletters / total) * 100) },
    { name: 'Notifications', count: breakdown.notifications, icon: '🔔', pct: Math.round((breakdown.notifications / total) * 100) },
    { name: 'Personal', count: breakdown.personal, icon: '👤', pct: Math.round((breakdown.personal / total) * 100) },
    { name: 'Finance', count: breakdown.finance, icon: '💰', pct: Math.round((breakdown.finance / total) * 100) },
    { name: 'Job/Recruitment', count: breakdown.jobRecruitment, icon: '💼', pct: Math.round((breakdown.jobRecruitment / total) * 100) },
  ];

  const filtered = selected === 'All' ? data.recentEmails : data.recentEmails.filter(e => e.aiCategory === selected);

  return (
    <div className="animate-fade-in max-w-5xl">
      <h1 className="text-2xl font-bold mb-6">🏷️ Email Categories</h1>

      {/* Category stats */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        {catStats.map(c => (
          <button
            key={c.name}
            onClick={() => setSelected(selected === c.name ? 'All' : c.name)}
            className={`card !p-4 text-left transition-all ${selected === c.name ? 'border-primary/50 glow' : ''}`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-2xl">{c.icon}</span>
              <span className={`category-badge border ${CATEGORY_COLORS[c.name]}`}>{c.pct}%</span>
            </div>
            <div className="font-semibold">{c.name}</div>
            <div className="text-sm text-text-muted">{c.count} emails</div>
            <div className="w-full h-1.5 bg-surface rounded-full mt-2 overflow-hidden">
              <div className="h-full rounded-full bg-gradient-to-r from-primary to-accent" style={{ width: `${c.pct}%` }} />
            </div>
          </button>
        ))}
      </div>

      {/* Filtered emails */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold">
          {selected === 'All' ? 'All Recent Emails' : selected} ({filtered.length})
        </h2>
        {selected !== 'All' && (
          <button onClick={() => setSelected('All')} className="text-sm text-primary hover:underline">
            Clear filter
          </button>
        )}
      </div>

      <div className="space-y-2">
        {filtered.length === 0 ? (
          <div className="card !p-8 text-center text-text-muted">No emails in this category.</div>
        ) : (
          filtered.map(email => (
            <Link key={email.id} href={`/dashboard/emails/${email.id}`} className="card !p-4 flex items-center gap-4">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary/30 to-accent/30 flex items-center justify-center font-semibold shrink-0">
                {email.senderName?.charAt(0) || email.senderEmail?.charAt(0) || '?'}
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-sm font-medium truncate">{email.subject || '(No Subject)'}</div>
                <div className="text-xs text-text-muted">
                  {email.senderName || email.senderEmail} • {email.aiSummary || email.snippet}
                </div>
              </div>
              {email.aiCategory && (
                <span className={`category-badge border shrink-0 ${CATEGORY_COLORS[email.aiCategory] || CATEGORY_COLORS['Uncategorized']}`}>
                  {CATEGORY_ICONS[email.aiCategory] || '📧'} {email.aiCategory}
                </span>
              )}
            </Link>
          ))
        )}
      </div>
    </div>
  );
}
