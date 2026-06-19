'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import type { Dashboard } from '@/lib/types';

export default function SettingsPage() {
  const router = useRouter();
  const [dashboardData, setDashboardData] = useState<Dashboard | null>(null);

  useEffect(() => {
    api.getDashboard()
      .then(setDashboardData)
      .catch(console.error);
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
      <h1 className="text-2xl font-bold mb-6">Settings</h1>

      <div className="card mb-6">
        <h2 className="text-lg font-semibold mb-4">Gmail Connection</h2>
        <div className="flex items-center gap-4 mb-4">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary/30 to-accent/30 flex items-center justify-center text-2xl">Mail</div>
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
          <div className="glass rounded-xl p-3">
            <div className="text-lg font-bold">{emailsSynced.toLocaleString()}</div>
            <div className="text-xs text-text-muted">Emails Synced</div>
          </div>
          <div className="glass rounded-xl p-3">
            <div className="text-lg font-bold">{totalThreads.toLocaleString()}</div>
            <div className="text-xs text-text-muted">Threads</div>
          </div>
          <div className="glass rounded-xl p-3">
            <div className="text-lg font-bold">{lastSync}</div>
            <div className="text-xs text-text-muted">Last Sync</div>
          </div>
        </div>
      </div>
    </div>
  );
}
