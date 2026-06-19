'use client';
import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { api } from '@/lib/api';
import { 
  LayoutDashboard, 
  Inbox, 
  Star, 
  Clock, 
  Send, 
  FileText, 
  Mail, 
  Trash2, 
  Tag, 
  Bookmark, 
  Brain,
  Sparkles, 
  PenSquare, 
  Reply, 
  Settings, 
  Layers 
} from 'lucide-react';

export default function Sidebar() {
  const pathname = usePathname();
  const [userEmail, setUserEmail] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [userAvatar, setUserAvatar] = useState('');
  const [unreadCount, setUnreadCount] = useState<number | null>(null);
  const [storageLimit, setStorageLimit] = useState<number>(15 * 1024 * 1024 * 1024);
  const [storageUsage, setStorageUsage] = useState<number>(0);

  useEffect(() => {
    // Try to load dynamic email/unread count if available
    api.getDashboard()
      .then(data => {
        if (data?.user?.gmailConnection?.gmailEmail) {
          setUserEmail(data.user.gmailConnection.gmailEmail);
        } else if (data?.user?.email) {
          setUserEmail(data.user.email);
        }
        if (data?.user?.displayName) {
          setDisplayName(data.user.displayName);
        }
        if (data?.user?.avatarUrl !== undefined) {
          setUserAvatar(data.user.avatarUrl);
        } else {
          setUserAvatar('');
        }
        if (data?.user?.gmailConnection?.storageLimit) {
          setStorageLimit(data.user.gmailConnection.storageLimit);
        }
        if (data?.user?.gmailConnection?.storageUsage) {
          setStorageUsage(data.user.gmailConnection.storageUsage);
        }
        if (data?.unreadEmails !== undefined) {
          setUnreadCount(data.unreadEmails);
        }
      })
      .catch(() => {
        // Fallback to demo defaults
      });
  }, []);

  const mainNavItems = [
    { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { href: '/dashboard/emails', label: 'Inbox', icon: Inbox, badge: unreadCount ?? undefined },
    { href: '/dashboard/starred', label: 'Starred', icon: Star },
    { href: '/dashboard/snoozed', label: 'Snoozed', icon: Clock },
    { href: '/dashboard/sent', label: 'Sent', icon: Send },
    { href: '/dashboard/drafts', label: 'Drafts', icon: FileText },
    { href: '/dashboard/all-mail', label: 'All Mail', icon: Mail },
    { href: '/dashboard/trash', label: 'Trash', icon: Trash2 },
    { href: '/dashboard/categories', label: 'Categories', icon: Tag },
    { href: '/dashboard/labels', label: 'Labels', icon: Bookmark },
  ];

  const aiToolsItems = [
    { href: '/dashboard/chat', label: 'AI Assistant', icon: Sparkles, badge: 'New', badgeColor: 'bg-[#6366f1]/20 text-[#818cf8] border-[#6366f1]/30' },
    { href: '/dashboard/compose', label: 'Compose', icon: PenSquare },
    { href: '/dashboard/reply', label: 'Smart Reply', icon: Reply },
  ];

  const settingsItems = [
    { href: '/dashboard/settings', label: 'Settings', icon: Settings },
    { href: '/dashboard/integrations', label: 'Integrations', icon: Layers },
  ];

  const isLinkActive = (href: string) => {
    if (href === '/dashboard') {
      return pathname === '/dashboard';
    }
    return pathname?.startsWith(href);
  };

  const formatBytesToGB = (bytes: number) => {
    return (bytes / (1024 * 1024 * 1024)).toFixed(1);
  };

  const storagePercentage = storageLimit > 0 ? (storageUsage / storageLimit) * 100 : 0;

  return (
    <aside className="fixed left-0 top-0 bottom-0 w-64 bg-[#090b11] border-r border-[#1a1f2e] z-40 flex flex-col font-sans select-none">
      {/* Brand Logo */}
      <Link href="/dashboard" className="flex items-center gap-3 px-6 py-5 border-b border-[#1a1f2e] shrink-0">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#6366f1] via-[#8b5cf6] to-[#06b6d4] flex items-center justify-center text-white shadow-[0_0_15px_rgba(99,102,241,0.4)]">
          <Brain className="w-5 h-5" />
        </div>
        <span className="text-[17px] font-bold tracking-tight text-white flex items-center gap-1.5">
          MailMind AI
        </span>
      </Link>

      {/* Navigation Links Area */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-6 scrollbar-thin">
        {/* Main Section */}
        <div className="space-y-1">
          {mainNavItems.map((item) => {
            const active = isLinkActive(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href === '/dashboard' || item.href === '/dashboard/emails' || item.href === '/dashboard/categories' ? item.href : '/dashboard'}
                className={`flex items-center justify-between px-3.5 py-2.5 rounded-xl text-[13.5px] font-medium transition-all duration-200 group relative
                  ${active 
                    ? 'bg-[#181d2a] text-[#818cf8] border border-[#2b354d]' 
                    : 'text-[#8c9bb4] hover:bg-[#111520] hover:text-white border border-transparent'
                  }`}
              >
                {active && (
                  <span className="absolute left-0 top-3 bottom-3 w-[3px] bg-[#6366f1] rounded-r-md" />
                )}
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 transition-colors ${active ? 'text-[#818cf8]' : 'text-[#8c9bb4] group-hover:text-white'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge !== undefined && (
                  <span className={`text-[11px] px-2 py-0.5 rounded-full font-semibold
                    ${active ? 'bg-[#6366f1]/20 text-[#818cf8]' : 'bg-[#181d2a] text-[#64748b]'}`}>
                    {item.badge}
                  </span>
                )}
              </Link>
            );
          })}
        </div>

        {/* AI Tools Section */}
        <div className="space-y-1.5">
          <div className="text-[10px] font-bold tracking-widest text-[#4e5e78] uppercase px-3.5 mb-2">
            AI Tools
          </div>
          {aiToolsItems.map((item) => {
            const active = isLinkActive(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href === '/dashboard/chat' || item.href === '/dashboard/compose' ? item.href : '/dashboard'}
                className={`flex items-center justify-between px-3.5 py-2.5 rounded-xl text-[13.5px] font-medium transition-all duration-200 group relative
                  ${active 
                    ? 'bg-[#181d2a] text-[#818cf8] border border-[#2b354d]' 
                    : 'text-[#8c9bb4] hover:bg-[#111520] hover:text-white border border-transparent'
                  }`}
              >
                {active && (
                  <span className="absolute left-0 top-3 bottom-3 w-[3px] bg-[#6366f1] rounded-r-md" />
                )}
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 transition-colors ${active ? 'text-[#818cf8]' : 'text-[#8c9bb4] group-hover:text-white'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge !== undefined && (
                  <span className="text-[9px] px-1.5 py-0.5 rounded-md font-bold bg-[#6366f1]/15 text-[#818cf8] border border-[#6366f1]/20">
                    {item.badge}
                  </span>
                )}
              </Link>
            );
          })}
        </div>

        {/* Settings Section */}
        <div className="space-y-1.5">
          <div className="text-[10px] font-bold tracking-widest text-[#4e5e78] uppercase px-3.5 mb-2">
            Settings
          </div>
          {settingsItems.map((item) => {
            const active = isLinkActive(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href === '/dashboard/settings' ? item.href : '/dashboard'}
                className={`flex items-center justify-between px-3.5 py-2.5 rounded-xl text-[13.5px] font-medium transition-all duration-200 group relative
                  ${active 
                    ? 'bg-[#181d2a] text-[#818cf8] border border-[#2b354d]' 
                    : 'text-[#8c9bb4] hover:bg-[#111520] hover:text-white border border-transparent'
                  }`}
              >
                {active && (
                  <span className="absolute left-0 top-3 bottom-3 w-[3px] bg-[#6366f1] rounded-r-md" />
                )}
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 transition-colors ${active ? 'text-[#818cf8]' : 'text-[#8c9bb4] group-hover:text-white'}`} />
                  <span>{item.label}</span>
                </div>
              </Link>
            );
          })}
        </div>
      </div>

      {/* Gmail Account widget at the bottom */}
      <div className="p-4 border-t border-[#1a1f2e] bg-[#0c0e16] shrink-0">
        <div className="flex items-center justify-between mb-3.5">
          <span className="text-[11px] font-bold tracking-wider text-[#4e5e78] uppercase">
            Gmail Account
          </span>
          <span className="flex items-center gap-1 text-[9px] font-bold px-1.5 py-0.5 rounded-md bg-[#10b981]/15 text-[#10b981] border border-[#10b981]/25">
            <span className="w-1.5 h-1.5 rounded-full bg-[#10b981] animate-pulse" />
            Active
          </span>
        </div>
        
        {/* Real-time User DP and Email section */}
        <div className="flex items-center gap-3 mb-4">
          {userAvatar ? (
            <img 
              src={userAvatar} 
              alt={displayName} 
              className="w-10 h-10 rounded-full object-cover border border-[#2b354d] shadow-sm"
              onError={() => {
                // If the google image fails to load, clear userAvatar state to fallback to initials
                setUserAvatar('');
              }}
            />
          ) : (
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-[#6366f1] via-[#8b5cf6] to-[#06b6d4] flex items-center justify-center text-white text-[14px] font-bold shadow-md">
              {displayName ? displayName.charAt(0).toUpperCase() : userEmail.charAt(0).toUpperCase()}
            </div>
          )}
          <div className="min-w-0 flex-1">
            <div className="text-[13px] font-semibold text-white truncate">
              {displayName}
            </div>
            <div className="text-[11px] text-[#8c9bb4] truncate select-text">
              {userEmail}
            </div>
          </div>
        </div>

        {/* Storage Quota progress */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between text-[10px] text-[#64748b]">
            <span>{formatBytesToGB(storageUsage)} GB of {formatBytesToGB(storageLimit)} GB used</span>
            <span className="font-semibold">{storagePercentage.toFixed(0)}%</span>
          </div>
          <div className="w-full h-1.5 bg-[#161a27] rounded-full overflow-hidden">
            <div className="h-full rounded-full bg-gradient-to-r from-[#8b5cf6] via-[#6366f1] to-[#06b6d4]" style={{ width: `${storagePercentage}%` }} />
          </div>
        </div>
      </div>
    </aside>
  );
}
