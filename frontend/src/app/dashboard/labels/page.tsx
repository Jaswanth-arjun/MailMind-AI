'use client';

import { useEffect, useState } from 'react';
import MailboxPage from '@/components/MailboxPage';
import { api } from '@/lib/api';

export default function LabelsPage() {
  const [labels, setLabels] = useState<string[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getLabels()
      .then(items => {
        setLabels(items);
        setSelected(items[0] || null);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="animate-fade-in max-w-5xl">
        <div className="mb-6 h-8 w-40 animate-pulse rounded bg-surface-lighter/50" />
        <div className="grid grid-cols-3 gap-3">
          {[...Array(6)].map((_, i) => <div key={i} className="h-12 animate-pulse rounded-xl bg-surface-lighter/50" />)}
        </div>
      </div>
    );
  }

  if (!selected) {
    return (
      <div className="animate-fade-in max-w-5xl">
        <h1 className="mb-6 text-2xl font-bold">Labels</h1>
        <div className="card !p-10 text-center text-text-muted">No custom Gmail labels found in synced mail.</div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in max-w-5xl">
      <h1 className="mb-4 text-2xl font-bold">Labels</h1>
      <div className="mb-6 flex flex-wrap gap-2">
        {labels.map(label => (
          <button
            key={label}
            onClick={() => setSelected(label)}
            className={`rounded-xl px-4 py-2 text-sm font-medium transition-all ${selected === label ? 'bg-primary text-white' : 'glass text-text-muted hover:text-text'}`}
          >
            {label}
          </button>
        ))}
      </div>
      <MailboxPage title={selected} icon="Label" label={selected} />
    </div>
  );
}
