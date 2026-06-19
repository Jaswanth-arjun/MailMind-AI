'use client';

import { useState, useEffect, useRef } from 'react';
import Sidebar from '@/components/Sidebar';
import FloatingChat from '@/components/FloatingChat';
import { api } from '@/lib/api';
import { Sparkles, X } from 'lucide-react';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<{ id: string; title: string; message: string; time: string }[]>([]);
  const knownIdsRef = useRef<Set<string>>(new Set());
  const initialLoadedRef = useRef<boolean>(false);

  useEffect(() => {
    // Initial fetch to populate known email IDs
    api.getEmails(0, 20)
      .then(res => {
        if (res && res.emails) {
          res.emails.forEach((email) => {
            knownIdsRef.current.add(email.id);
          });
        }
        initialLoadedRef.current = true;
      })
      .catch(console.error);

    // Polling interval to check for new synced emails
    const pollInterval = setInterval(() => {
      api.getEmails(0, 10)
        .then(res => {
          if (!res || !res.emails) return;

          let hasNew = false;
          res.emails.forEach((email) => {
            if (!knownIdsRef.current.has(email.id)) {
              knownIdsRef.current.add(email.id);

              // Only show toast if initial load is completed
              if (initialLoadedRef.current) {
                hasNew = true;
                const toastId = `${email.id}-${Date.now()}`;
                setToasts(prev => [
                  ...prev,
                  {
                    id: toastId,
                    title: `New Email: ${email.senderName || email.senderEmail}`,
                    message: email.subject || 'No Subject',
                    time: 'Just now'
                  }
                ]);

                // Auto remove toast after 5 seconds
                setTimeout(() => {
                  setToasts(prev => prev.filter(t => t.id !== toastId));
                }, 5000);
              }
            }
          });

          // If new emails were found, dispatch a global custom event to notify active pages to reload
          if (hasNew) {
            window.dispatchEvent(new CustomEvent('new-email-received'));
          }
        })
        .catch(console.error);

    }, 8000); // Poll every 8 seconds

    return () => clearInterval(pollInterval);
  }, []);

  return (
    <div className="flex min-h-screen bg-[#06080f] relative">
      <Sidebar />
      <main className="flex-1 ml-64 p-8 overflow-y-auto min-h-screen">
        {children}
      </main>
      <FloatingChat />

      {/* Floating Toast Notification Container */}
      <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 w-80 max-w-full pointer-events-none">
        {toasts.map(t => (
          <div key={t.id} className="pointer-events-auto bg-[#0f111a]/95 border border-[#6366f1]/30 text-white p-4 rounded-xl shadow-2xl backdrop-blur-md flex gap-3 animate-slide-in-right animate-fade-in relative group">
            <div className="w-8 h-8 rounded-lg bg-[#6366f1]/20 flex items-center justify-center text-[#818cf8] shrink-0">
              <Sparkles className="w-4 h-4 animate-pulse" />
            </div>
            <div className="min-w-0 flex-1 pr-6">
              <div className="text-[11.5px] font-bold flex justify-between items-center gap-2">
                <span className="truncate text-white">{t.title}</span>
                <span className="text-[#818cf8] text-[10px] font-semibold shrink-0">{t.time}</span>
              </div>
              <div className="text-[10.5px] text-[#8c9bb4] mt-1 truncate">{t.message}</div>
            </div>
            <button
              onClick={() => setToasts(prev => prev.filter(item => item.id !== t.id))}
              className="absolute top-3 right-3 text-[#64748b] hover:text-white transition-colors cursor-pointer p-0.5"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
