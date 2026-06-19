'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import type { Dashboard } from '@/lib/types';

export default function SettingsPage() {
  const router = useRouter();
  const [dashboardData, setDashboardData] = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getDashboard()
      .then(setDashboardData)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleDisconnect = () => {
    localStorage.removeItem('mailmind_token');
    router.push('/');
  };

  const gmailEmail = dashboardData?.user?.gmailConnection?.gmailEmail || dashboardData?.user?.email || '';
  const isConnected = dashboardData?.user?.gmailConnection?.connected ?? false;
  const emailsSynced = dashboardData?.user?.gmailConnection?.totalEmailsSynced ?? 0;
  const totalThreads = dashboardData?.totalThreads ?? 0;
  const lastSync = dashboardData?.user?.gmailConnection?.lastSyncAt 
    ? new Date(dashboardData.user.gmailConnection.lastSyncAt).toLocaleString() 
    : 'Never synced';

  return (
    <div className="animate-fade-in max-w-3xl">
      <h1 className="text-2xl font-bold mb-6">⚙️ Settings</h1>

      {/* Gmail Connection */}
      <div className="card mb-6">
        <h2 className="text-lg font-semibold mb-4">Gmail Connection</h2>
        <div className="flex items-center gap-4 mb-4">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary/30 to-accent/30 flex items-center justify-center text-2xl">📧</div>
          <div className="flex-1">
            <div className="font-medium">{gmailEmail}</div>
            <div className="text-sm text-text-muted flex items-center gap-2">
              <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-success' : 'bg-danger'}`} /> 
              {isConnected ? 'Connected' : 'Disconnected'}
            </div>
          </div>
          <button onClick={handleDisconnect} className="btn-secondary text-sm">Disconnect</button>
        </div>
        <div className="grid grid-cols-3 gap-4 text-center">
          <div className="glass rounded-xl p-3"><div className="text-lg font-bold">{emailsSynced.toLocaleString()}</div><div className="text-xs text-text-muted">Emails Synced</div></div>
          <div className="glass rounded-xl p-3"><div className="text-lg font-bold">{totalThreads.toLocaleString()}</div><div className="text-xs text-text-muted">Threads</div></div>
          <div className="glass rounded-xl p-3"><div className="text-lg font-bold">{lastSync}</div><div className="text-xs text-text-muted">Last Sync</div></div>
        </div>
      </div>

      {/* API Configuration */}
      <div className="card mb-6">
        <h2 className="text-lg font-semibold mb-4">API Configuration</h2>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium text-text-muted block mb-1">Backend API URL</label>
            <input className="input-field" defaultValue={process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"} readOnly />
          </div>
        </div>
      </div>

      {/* AI Models */}
      <div className="card mb-6">
        <h2 className="text-lg font-semibold mb-4">AI Models</h2>
        <div className="space-y-3">
          <div className="flex items-center justify-between glass rounded-xl p-4">
            <div>
              <div className="font-medium text-sm">Primary: Google Gemini 1.5 Flash</div>
              <div className="text-xs text-text-muted">Summarization, categorization, reply generation, embeddings</div>
            </div>
            <span className="text-xs text-success font-medium">Active</span>
          </div>
          <div className="flex items-center justify-between glass rounded-xl p-4">
            <div>
              <div className="font-medium text-sm">Secondary: NVIDIA NIM (LLaMA 3.1 8B)</div>
              <div className="text-xs text-text-muted">Fallback model when Gemini is unavailable</div>
            </div>
            <span className="text-xs text-text-dim font-medium">Standby</span>
          </div>
        </div>
      </div>

      {/* Environment Variables */}
      <div className="card">
        <h2 className="text-lg font-semibold mb-4">Environment Variables</h2>
        <div className="space-y-2 text-sm">
          {['GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET', 'GEMINI_API_KEY', 'NVIDIA_NIM_API_KEY', 'SUPABASE_URL', 'APP_JWT_SECRET'].map(v => (
            <div key={v} className="flex items-center justify-between py-2 border-b border-border/50">
              <code className="text-text-muted">{v}</code>
              <span className="text-xs text-text-dim">••••••••</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
