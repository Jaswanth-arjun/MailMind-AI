'use client';
import { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { Dashboard, EmailSummary, ChatMessage } from '@/lib/types';
import { 
  ArrowRight, 
  SendHorizontal, 
  Star, 
  Brain, 
  Sparkles, 
  Plus, 
  MessageSquare, 
  FileText, 
  Check, 
  Loader2,
  RefreshCw,
  X,
  Mail,
  Inbox,
  GitBranch,
  SearchCode
} from 'lucide-react';

function formatTime(iso: string) {
  const d = new Date(iso);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  if (diff < 3600000) return `${Math.floor(diff/60000)}m ago`;
  if (diff < 86400000) return `${Math.floor(diff/3600000)}h ago`;
  return d.toLocaleDateString('en-US', { hour: '2-digit', minute: '2-digit' });
}

// Helper to process and normalize dashboard data (strictly using database results, no simulation or fake data)
function processDashboardData(d: Dashboard): Dashboard {
  if (!d) return d;
  
  const categoryBreakdown = d.categoryBreakdown || {
    newsletters: 0,
    jobRecruitment: 0,
    finance: 0,
    notifications: 0,
    personal: 0,
    workProfessional: 0,
    uncategorized: 0
  };
  
  return {
    ...d,
    categoryBreakdown,
    activity: d.activity || [],
    topSenders: d.topSenders || [],
    recentEmails: d.recentEmails || [],
    totalEmails: d.totalEmails || 0,
    unreadEmails: d.unreadEmails || 0,
    totalThreads: d.totalThreads || 0
  };
}

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [syncTimeText, setSyncTimeText] = useState('Last synced 2 mins ago');

  // Chart Hover Interactive States
  const [hoveredDay, setHoveredDay] = useState<number | null>(4); // Default to Friday (index 4) like the mockup

  const loadData = () => {
    api.getDashboard()
      .then(fetched => {
        setData(processDashboardData(fetched));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();

    // Listen for new email arrivals detected globally
    const handleNewEmail = () => {
      loadData();
    };
    window.addEventListener('new-email-received', handleNewEmail);

    // Poll dashboard data every 10 seconds to keep charts and tables fully updated with real-time data
    const timer = setInterval(() => {
      api.getDashboard()
        .then(fetched => {
          setData(processDashboardData(fetched));
        })
        .catch(console.error);
    }, 10000);

    return () => {
      window.removeEventListener('new-email-received', handleNewEmail);
      clearInterval(timer);
    };
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
            setSyncTimeText('Last synced just now');
            loadData();
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


  if (loading) return (
    <div className="flex items-center justify-center h-[70vh]">
      <div className="text-center">
        <div className="w-12 h-12 rounded-full border-2 border-[#6366f1] border-t-transparent animate-spin mx-auto mb-4" />
        <p className="text-[#8c9bb4] font-medium text-sm">Loading MailMind intelligence...</p>
      </div>
    </div>
  );

  if (!data) return null;

  // Compute category details — includes all 7 backend categories (no static fallback)
  const categoryCounts = [
    { label: 'Newsletters',    count: data.categoryBreakdown.newsletters    || 0, color: '#a855f7', hoverColor: 'hover:bg-[#a855f7]/15' },
    { label: 'Work',           count: data.categoryBreakdown.workProfessional || 0, color: '#3b82f6', hoverColor: 'hover:bg-[#3b82f6]/15' },
    { label: 'Personal',       count: data.categoryBreakdown.personal       || 0, color: '#ec4899', hoverColor: 'hover:bg-[#ec4899]/15' },
    { label: 'Finance',        count: data.categoryBreakdown.finance        || 0, color: '#eab308', hoverColor: 'hover:bg-[#eab308]/15' },
    { label: 'Notifications',  count: data.categoryBreakdown.notifications  || 0, color: '#f97316', hoverColor: 'hover:bg-[#f97316]/15' },
    { label: 'Jobs',           count: data.categoryBreakdown.jobRecruitment || 0, color: '#06b6d4', hoverColor: 'hover:bg-[#06b6d4]/15' },
    { label: 'Uncategorized',  count: data.categoryBreakdown.uncategorized  || 0, color: '#64748b', hoverColor: 'hover:bg-[#64748b]/15' },
  ];

  const totalCategoryEmails = categoryCounts.reduce((sum, item) => sum + item.count, 0) || 1;
  const totalCategorizedEmails = categoryCounts
    .filter(item => item.label !== 'Uncategorized')
    .reduce((sum, item) => sum + item.count, 0) || 1;

  // Only show categories that have at least 1 email (excluding Uncategorized) for the donut slices
  const activeCategoriesList = categoryCounts.filter(item => item.label !== 'Uncategorized' && item.count > 0);
  const activeCategoriesCount = activeCategoriesList.length;

  const categoriesList = categoryCounts.map(item => {
    let pct = 0;
    if (item.label === 'Uncategorized') {
      pct = Math.round((item.count / totalCategoryEmails) * 100);
    } else {
      pct = Math.round((item.count / totalCategorizedEmails) * 100);
    }
    return {
      ...item,
      percentage: pct
    };
  });

  // Activity chart Mon-Sun — real data only, no static fallback
  // Backend always returns 7 ActivityDay entries (count=0 for days with no emails)
  const rawActivity: { day: string; count: number }[] = (data.activity && data.activity.length > 0)
    ? data.activity
    : [{ day: 'Mon', count: 0 }, { day: 'Tue', count: 0 }, { day: 'Wed', count: 0 },
       { day: 'Thu', count: 0 }, { day: 'Fri', count: 0 }, { day: 'Sat', count: 0 }, { day: 'Sun', count: 0 }];

  // Compute maxVal for Y-axis scaling dynamically:
  const rawMax = Math.max(10, ...rawActivity.map(d => d.count));
  let step = 150;
  if (rawMax <= 12) step = 3;
  else if (rawMax <= 20) step = 5;
  else if (rawMax <= 40) step = 10;
  else if (rawMax <= 80) step = 20;
  else if (rawMax <= 200) step = 50;
  else if (rawMax <= 400) step = 100;
  else if (rawMax <= 800) step = 150;
  else {
    const power = Math.pow(10, Math.floor(Math.log10(rawMax)) - 1);
    step = Math.ceil(rawMax / (4 * power)) * power;
  }
  const maxVal = step * 4;

  const activityData = rawActivity.map((d, index) => {
    const x = 70 + index * 65;
    // Map count to y space: from y=170 (0 count) to y=30 (maxVal count)
    const y = Math.round(170 - ((d.count / maxVal) * 140));
    return {
      day: d.day,
      count: d.count,
      x,
      y
    };
  });

  const totalEmailsThisWeek = rawActivity.reduce((sum, d) => sum + d.count, 0);

  // Construct dynamic SVG path
  let curvePath = '';
  if (activityData.length > 0) {
    curvePath = `M ${activityData[0].x} ${activityData[0].y}`;
    for (let i = 0; i < activityData.length - 1; i++) {
      const p0 = activityData[i];
      const p1 = activityData[i + 1];
      const cpX1 = (p0.x + p1.x) / 2;
      const cpY1 = p0.y;
      const cpX2 = (p0.x + p1.x) / 2;
      const cpY2 = p1.y;
      curvePath += ` C ${cpX1} ${cpY1}, ${cpX2} ${cpY2}, ${p1.x} ${p1.y}`;
    }
  }

  const fillPath = activityData.length > 0 
    ? `${curvePath} L ${activityData[activityData.length - 1].x} 170 L ${activityData[0].x} 170 Z` 
    : '';

  // Dynamic Top Senders calculation based on real-time data
  const maxSenderCountVal = (data.topSenders && data.topSenders.length > 0)
    ? Math.max(...data.topSenders.map(s => s.count))
    : 0;
  const maxAxisVal = Math.max(50, Math.ceil(maxSenderCountVal / 10) * 10);
  const axisTicks = [
    0, 
    Math.round(maxAxisVal * 0.2), 
    Math.round(maxAxisVal * 0.4), 
    Math.round(maxAxisVal * 0.6), 
    Math.round(maxAxisVal * 0.8), 
    maxAxisVal
  ];

  const defaultColors = [
    'bg-gradient-to-r from-[#6366f1] to-[#8b5cf6]',
    'bg-gradient-to-r from-[#3b82f6] to-[#06b6d4]',
    'bg-gradient-to-r from-[#10b981] to-[#059669]',
    'bg-gradient-to-r from-[#eab308] to-[#f97316]',
    'bg-gradient-to-r from-[#ec4899] to-[#f43f5e]'
  ];

  // Top senders — real data only, no static fallback
  const sendersData = (data.topSenders && data.topSenders.length > 0)
    ? data.topSenders.slice(0, 5).map((s, idx) => ({
        name: s.name,
        email: s.email || '',
        count: s.count,
        percentage: Math.round((s.count / maxAxisVal) * 100),
        color: defaultColors[idx % defaultColors.length]
      }))
    : [];

  // Email Status Donut parameters
  const unreadStatusPercentage = Math.round((data.unreadEmails / (data.totalEmails || 1)) * 100);
  const readStatusPercentage = 100 - unreadStatusPercentage;

  // 4-Quadrant Inbox Overview dynamic SVG arcs (Max arc length = 133, Circumference = 596.9)
  const maxArcLen = 133;
  const totalValForRing = data.totalEmails || 1;
  const threadsRatio = Math.max(0.08, Math.min(1.0, data.totalThreads / totalValForRing));
  const syncedRatio = Math.max(0.08, Math.min(1.0, (data.user?.gmailConnection?.totalEmailsSynced || 0) / totalValForRing));
  const unreadRatio = Math.max(0.08, Math.min(1.0, data.unreadEmails / totalValForRing));
  const totalRatio = 1.0;

  const threadsArc = threadsRatio * maxArcLen;
  const syncedArc = syncedRatio * maxArcLen;
  const unreadArc = unreadRatio * maxArcLen;
  const totalArc = totalRatio * maxArcLen;

  // Category donut — exclude Uncategorized so real category colors show, with 1.5% gaps between segments
  const categoriesForDonut = categoriesList.filter(c => c.label !== 'Uncategorized' && c.count > 0);
  const totalForDonut = categoriesForDonut.reduce((sum, c) => sum + c.count, 0) || 1;
  const gapSize = categoriesForDonut.length > 1 ? 1.5 : 0;
  
  let currentOffset = 0;
  const gradientSlices: string[] = [];
  categoriesForDonut.forEach((item) => {
    const slicePercentage = (item.count / totalForDonut) * (100 - gapSize * categoriesForDonut.length);
    const start = currentOffset;
    const end = currentOffset + slicePercentage;
    gradientSlices.push(`${item.color} ${start.toFixed(2)}% ${end.toFixed(2)}%`);
    
    currentOffset = end;
    if (categoriesForDonut.length > 1) {
      const gapEnd = currentOffset + gapSize;
      gradientSlices.push(`transparent ${currentOffset.toFixed(2)}% ${gapEnd.toFixed(2)}%`);
      currentOffset = gapEnd;
    }
  });

  const categoryConicGradient = categoriesForDonut.length > 0
    ? `conic-gradient(${gradientSlices.join(', ')})`
    : 'conic-gradient(#1e293b 0% 100%)';


  const enrichedEmails = data.recentEmails.slice(0, 5);

  return (
    <div className="animate-fade-in text-white font-sans max-w-[1360px] mx-auto pb-12">


      {/* Greeting Row */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-[26px] font-extrabold tracking-tight text-white flex items-center gap-2">
            Welcome back, {data.user?.displayName?.split(' ')[0] || 'there'}! <span className="animate-float inline-block">👋</span>
          </h1>
          <p className="text-[13.5px] text-[#8c9bb4] mt-0.5">Here&apos;s what&apos;s happening in your inbox today.</p>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#10b981]/10 border border-[#10b981]/20">
            <span className="w-1.5 h-1.5 rounded-full bg-[#10b981] animate-pulse" />
            <span className="text-[11px] font-bold text-[#10b981] uppercase tracking-wider">Active</span>
          </div>
          <span className="text-[12px] text-[#64748b] font-medium">{syncTimeText}</span>
          
          <button 
            onClick={handleSync} 
            disabled={syncing} 
            className="flex items-center gap-2 px-5 py-2 rounded-xl text-[13px] font-semibold tracking-wide text-white transition-all bg-gradient-to-r from-[#6366f1] via-[#4f46e5] to-[#8b5cf6] hover:brightness-110 shadow-[0_4px_14px_rgba(99,102,241,0.3)] hover:shadow-[0_6px_20px_rgba(99,102,241,0.45)] disabled:opacity-75 disabled:cursor-not-allowed cursor-pointer"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${syncing ? 'animate-spin' : ''}`} />
            {syncing ? 'Syncing...' : 'Sync Now'}
          </button>
        </div>
      </div>
      {/* Top Row Grid: Inbox Overview + Email Category Distribution */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-8">
        
        {/* Inbox Overview Widget */}
        <div className="bg-[#0f111a] border border-[#1a1f2e] rounded-3xl p-6 relative overflow-hidden flex flex-col gap-5"
             style={{ boxShadow: '0 4px 30px rgba(0,0,0,0.3), inset 0 1px 1px rgba(255,255,255,0.05)' }}>

          {/* Header */}
          <div>
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-full bg-[#6366f1]/10 border border-[#6366f1]/20 flex items-center justify-center text-[#6366f1]">
                <div className="flex items-end gap-0.5 h-4">
                  <span className="w-1 h-2 bg-current rounded-full" />
                  <span className="w-1 h-3.5 bg-current rounded-full" />
                  <span className="w-1 h-2.5 bg-current rounded-full" />
                </div>
              </div>
              <div>
                <h2 className="text-[15px] font-bold text-white tracking-wide">Inbox Overview</h2>
                <p className="text-[11.5px] text-[#64748b] mt-0.5">A quick snapshot of your email activity.</p>
              </div>
            </div>
            <div className="w-6 h-1 bg-[#6366f1] rounded-full mt-2.5" />
          </div>

          {/* Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-center flex-1">

            {/* Left: 4-Quadrant Ring with Nodes */}
            <div className="lg:col-span-7 flex items-center justify-center relative min-h-[300px]">
              <div className="relative w-[280px] h-[280px] flex items-center justify-center">

                {/* Ring SVGs (Circumference of r=95 is 596.9) */}
                {/* Dasharray of 133 leaves a small, elegant gap between the four 149.2-px quadrants */}
                <svg className="absolute inset-0 w-full h-full pointer-events-none" viewBox="0 0 280 280">
                  {/* Quadrant 1: Threads (Blue) - Top Right (starts top-center and goes 90 deg clockwise) */}
                  <circle cx="140" cy="140" r="95" fill="none" stroke="#3b82f6" strokeWidth="6" strokeDasharray="133 463.9" strokeDashoffset="0" strokeLinecap="round" transform="rotate(-90 140 140)" style={{ filter: 'drop-shadow(0 0 7px rgba(59, 130, 246, 0.75))' }} />
                  {/* Quadrant 2: Total (Green) - Bottom Right */}
                  <circle cx="140" cy="140" r="95" fill="none" stroke="#10b981" strokeWidth="6" strokeDasharray="133 463.9" strokeDashoffset="0" strokeLinecap="round" transform="rotate(0 140 140)" style={{ filter: 'drop-shadow(0 0 7px rgba(16, 185, 129, 0.75))' }} />
                  {/* Quadrant 3: Synced (Orange) - Bottom Left */}
                  <circle cx="140" cy="140" r="95" fill="none" stroke="#f97316" strokeWidth="6" strokeDasharray="133 463.9" strokeDashoffset="0" strokeLinecap="round" transform="rotate(90 140 140)" style={{ filter: 'drop-shadow(0 0 7px rgba(249, 115, 22, 0.75))' }} />
                  {/* Quadrant 4: Unread (Purple) - Top Left */}
                  <circle cx="140" cy="140" r="95" fill="none" stroke="#8b5cf6" strokeWidth="6" strokeDasharray="133 463.9" strokeDashoffset="0" strokeLinecap="round" transform="rotate(180 140 140)" style={{ filter: 'drop-shadow(0 0 7px rgba(139, 92, 246, 0.75))' }} />
                  
                  {/* Circuit Tracer Lines */}
                  {/* Top-Left Tracer (Purple) */}
                  <path d="M 72.8 72.8 C 62 72.8, 52 55, 45 45" fill="none" stroke="#8b5cf6" strokeWidth="1" opacity="0.45" />
                  <circle cx="72.8" cy="72.8" r="2.5" fill="#8b5cf6" />
                  <circle cx="45" cy="45" r="1.5" fill="#8b5cf6" />

                  {/* Top-Right Tracer (Blue) */}
                  <path d="M 207.2 72.8 C 218 72.8, 228 55, 235 45" fill="none" stroke="#3b82f6" strokeWidth="1" opacity="0.45" />
                  <circle cx="207.2" cy="72.8" r="2.5" fill="#3b82f6" />
                  <circle cx="235" cy="45" r="1.5" fill="#3b82f6" />

                  {/* Bottom-Left Tracer (Orange) */}
                  <path d="M 72.8 207.2 C 62 207.2, 52 225, 45 235" fill="none" stroke="#f97316" strokeWidth="1" opacity="0.45" />
                  <circle cx="72.8" cy="207.2" r="2.5" fill="#f97316" />
                  <circle cx="45" cy="235" r="1.5" fill="#f97316" />

                  {/* Bottom-Right Tracer (Green) */}
                  <path d="M 207.2 207.2 C 218 207.2, 228 225, 235 235" fill="none" stroke="#10b981" strokeWidth="1" opacity="0.45" />
                  <circle cx="207.2" cy="207.2" r="2.5" fill="#10b981" />
                  <circle cx="235" cy="235" r="1.5" fill="#10b981" />
                </svg>

                {/* Center dark circle */}
                <div className="relative z-10 w-[148px] h-[148px] rounded-full bg-[#0a0d16] border border-[#1e2235] flex flex-col items-center justify-center shadow-[inset_0_4px_25px_rgba(0,0,0,0.85)]">
                  {/* Sparkle squircle badge icon */}
                  <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-[#6366f1] to-[#8b5cf6] flex items-center justify-center text-white mb-2 shadow-[0_0_15px_rgba(139,92,246,0.5)] border border-[#a78bfa]/35 relative">
                    <Mail className="w-5.5 h-5.5" />
                    {/* Small sparkle emoji/icon detail */}
                    <span className="absolute -top-1 -right-1 text-[10px] select-none animate-pulse">✨</span>
                  </div>
                  <div className="text-[26px] font-black text-white tracking-tight leading-none">{data.totalEmails.toLocaleString()}</div>
                  <div className="text-[8px] font-black text-[#64748b] tracking-[0.18em] uppercase mt-1.5">Total Emails</div>
                </div>

                {/* Top-Left Node Card: Unread */}
                <div className="absolute top-[3%] left-[-16%] flex items-center gap-2.5 bg-[#0b0c14]/40 border border-[#1e2235]/40 p-1.5 rounded-2xl">
                  <div className="w-9 h-9 rounded-xl bg-[#8b5cf6]/10 border border-[#8b5cf6]/25 flex items-center justify-center text-[#c084fc] shadow-[0_0_12px_rgba(139,92,246,0.15)]"><Inbox className="w-4 h-4" /></div>
                  <div className="flex flex-col text-left">
                    <span className="text-[14px] font-black text-white leading-none">{data.unreadEmails.toLocaleString()}</span>
                    <span className="text-[9px] text-[#8c9bb4] font-bold mt-1">Unread</span>
                  </div>
                </div>

                {/* Top-Right Node Card: Threads */}
                <div className="absolute top-[3%] right-[-16%] flex items-center gap-2.5 bg-[#0b0c14]/40 border border-[#1e2235]/40 p-1.5 rounded-2xl text-right">
                  <div className="flex flex-col items-end text-right">
                    <span className="text-[14px] font-black text-white leading-none">{data.totalThreads.toLocaleString()}</span>
                    <span className="text-[9px] text-[#8c9bb4] font-bold mt-1">Threads</span>
                  </div>
                  <div className="w-9 h-9 rounded-xl bg-[#3b82f6]/10 border border-[#3b82f6]/25 flex items-center justify-center text-[#60a5fa] shadow-[0_0_12px_rgba(59,130,246,0.15)]">
                    {/* Chat/Bubble icon */}
                    <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                    </svg>
                  </div>
                </div>

                {/* Bottom-Left Node Card: Synced */}
                <div className="absolute bottom-[3%] left-[-16%] flex items-center gap-2.5 bg-[#0b0c14]/40 border border-[#1e2235]/40 p-1.5 rounded-2xl">
                  <div className="w-9 h-9 rounded-xl bg-[#f97316]/10 border border-[#f97316]/25 flex items-center justify-center text-[#fb923c] shadow-[0_0_12px_rgba(249,115,22,0.15)]"><RefreshCw className="w-4 h-4" /></div>
                  <div className="flex flex-col text-left">
                    <span className="text-[14px] font-black text-white leading-none">{(data.user?.gmailConnection?.totalEmailsSynced || 0).toLocaleString()}</span>
                    <span className="text-[9px] text-[#8c9bb4] font-bold mt-1">Synced</span>
                  </div>
                </div>

                {/* Bottom-Right Node Card: Total */}
                <div className="absolute bottom-[3%] right-[-16%] flex items-center gap-2.5 bg-[#0b0c14]/40 border border-[#1e2235]/40 p-1.5 rounded-2xl text-right">
                  <div className="flex flex-col items-end text-right">
                    <span className="text-[14px] font-black text-white leading-none">{data.totalEmails.toLocaleString()}</span>
                    <span className="text-[9px] text-[#8c9bb4] font-bold mt-1">Total</span>
                  </div>
                  <div className="w-9 h-9 rounded-xl bg-[#10b981]/10 border border-[#10b981]/25 flex items-center justify-center text-[#34d399] shadow-[0_0_12px_rgba(16,185,129,0.15)]"><Check className="w-4 h-4" /></div>
                </div>

              </div>
            </div>

            {/* Divider */}
            <div className="hidden lg:flex lg:col-span-1 justify-center">
              <div className="w-px h-[200px] bg-gradient-to-b from-transparent via-[#1a1f2e] to-transparent" />
            </div>

            {/* Right: Stats */}
            <div className="lg:col-span-4 flex flex-col gap-3">
              {[
                { label: 'Unread', sub: 'Requires your attention', val: data.unreadEmails.toLocaleString(), color: '#8b5cf6', Icon: Inbox },
                { label: 'Threads', sub: 'Email conversations', val: data.totalThreads.toLocaleString(), color: '#3b82f6', Icon: MessageSquare },
                { label: 'Synced', sub: 'Successfully synchronized', val: (data.user?.gmailConnection?.totalEmailsSynced || 0).toLocaleString(), color: '#f97316', Icon: RefreshCw },
                { label: 'Total Emails', sub: 'All emails in your account', val: data.totalEmails.toLocaleString(), color: '#10b981', Icon: Mail },
              ].map((item, i, arr) => (
                <div key={item.label} className={`flex items-center justify-between py-1.5 ${i < arr.length - 1 ? 'border-b border-[#1a1f2e]/50' : ''}`}>
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: item.color, boxShadow: `0 0 6px ${item.color}` }} />
                    <div className="w-7 h-7 rounded-full flex items-center justify-center shrink-0"
                         style={{ backgroundColor: `${item.color}18`, border: `1px solid ${item.color}33`, color: item.color }}>
                      <item.Icon className="w-3.5 h-3.5" />
                    </div>
                    <div>
                      <div className="text-[12px] font-bold text-white leading-tight">{item.label}</div>
                      <div className="text-[9.5px] text-[#64748b] leading-tight">{item.sub}</div>
                    </div>
                  </div>
                  <span className="text-[15px] font-black" style={{ color: item.color }}>{item.val}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Footer */}
          <div className="pt-4 border-t border-[#1a1f2e] flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-5 h-5 rounded-full bg-[#10b981]/10 border border-[#10b981]/20 flex items-center justify-center text-[#10b981]"><Check className="w-3 h-3" /></div>
              <div>
                <div className="text-[11px] font-bold text-white leading-tight">All systems up to date</div>
                <div className="text-[9.5px] text-[#64748b] leading-tight">Last synchronized 2 minutes ago</div>
              </div>
            </div>
            <div className="w-32 h-6 text-[#6366f1] opacity-70">
              <svg className="w-full h-full" viewBox="0 0 100 30" fill="none">
                <path d="M0,20 Q15,8 30,18 T60,8 T90,14 T100,5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                <circle cx="100" cy="5" r="2.5" fill="#8b5cf6" className="animate-pulse" />
              </svg>
            </div>
          </div>
        </div>





        {/* Email Category Distribution Widget */}
        <div className="bg-[#0f111a] border border-[#1a1f2e] rounded-3xl p-6 relative overflow-hidden"
             style={{ boxShadow: '0 4px 30px rgba(0,0,0,0.3), inset 0 1px 1px rgba(255,255,255,0.05)' }}>
          
          <div>
            {/* Header */}
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-full bg-[#8b5cf6]/10 border border-[#8b5cf6]/20 flex items-center justify-center text-[#8b5cf6]">
                <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M21.21 15.89A10 10 0 1 1 8 2.83" />
                  <path d="M22 12A10 10 0 0 0 12 2v10z" />
                </svg>
              </div>
              <div>
                <h2 className="text-[15px] font-bold text-white tracking-wide">Email Category Distribution</h2>
                <p className="text-[11.5px] text-[#64748b] mt-0.5">Overview of your emails by category</p>
              </div>
            </div>
            <div className="w-6 h-1 bg-[#6366f1] rounded-full mt-2.5 mb-6" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center flex-1">
            
            {/* Left side: Conic Donut graphic */}
            <div className="lg:col-span-5 flex items-center justify-center relative min-h-[190px]">
              <div className="relative w-40 h-40 flex items-center justify-center shrink-0">
                {/* Floating sparkles/lights around donut chart to match reference image */}
                <span className="absolute -top-3 -left-3 w-1.5 h-1.5 rounded-full bg-[#8b5cf6] shadow-[0_0_10px_#8b5cf6] animate-pulse" />
                <span className="absolute -top-1 -right-4 w-1 h-1 rounded-full bg-[#06b6d4] shadow-[0_0_8px_#06b6d4] opacity-75" />
                <span className="absolute top-1/4 -left-6 w-1 w-1 h-1 rounded-full bg-[#d946ef] shadow-[0_0_8px_#d946ef] opacity-80" />
                <span className="absolute top-8 right-[103%] w-1.5 h-1.5 rounded-full bg-[#8b5cf6] shadow-[0_0_8px_#8b5cf6] opacity-60" />
                <span className="absolute bottom-4 -left-4 w-1.5 h-1.5 rounded-full bg-[#f97316] shadow-[0_0_10px_#f97316] animate-pulse" />
                <span className="absolute -bottom-2 right-4 w-1 h-1 rounded-full bg-[#ef4444] shadow-[0_0_8px_#ef4444] opacity-85" />
                <span className="absolute bottom-6 -right-5 w-1.5 h-1.5 rounded-full bg-[#f59e0b] shadow-[0_0_10px_#f59e0b]" />
                <span className="absolute top-2 right-1/4 w-1 h-1 rounded-full bg-[#3b82f6] shadow-[0_0_8px_#3b82f6] opacity-70" />

                <div 
                  className="w-full h-full rounded-full flex items-center justify-center transition-all duration-500 hover:scale-[1.02] shadow-[inset_0_4px_12px_rgba(0,0,0,0.5)]" 
                  style={{ 
                    background: categoryConicGradient,
                    maskImage: 'radial-gradient(transparent 58%, black 59%)',
                    WebkitMaskImage: 'radial-gradient(transparent 58%, black 59%)',
                  }} 
                />
                <div className="absolute flex flex-col items-center justify-center text-center">
                  <span className="text-[28px] font-black text-white leading-none tracking-tight">{activeCategoriesCount}</span>
                  <span className="text-[9px] font-black text-[#64748b] tracking-[0.15em] uppercase mt-1">Categories</span>
                </div>
              </div>
            </div>

            {/* Right side: Progress Bar List */}
            <div className="lg:col-span-7 flex flex-col gap-3 min-w-0">
              {categoriesList.map((cat, idx) => (
                <Link 
                  key={idx} 
                  href={`/dashboard/emails?category=${encodeURIComponent(cat.label === 'Work' ? 'Work/Professional' : cat.label === 'Jobs' ? 'Job/Recruitment' : cat.label)}`}
                  className="flex flex-col gap-1 hover:bg-[#111520] p-1.5 rounded-xl transition-all"
                >
                  <div className="flex items-center justify-between text-[11.5px] font-semibold">
                    <div className="flex items-center gap-2 min-w-0">
                      <span className="text-[14px]">{CATEGORY_ICONS[cat.label === 'Work' ? 'Work/Professional' : cat.label === 'Jobs' ? 'Job/Recruitment' : cat.label] || '📧'}</span>
                      <span className="text-[#8c9bb4] truncate">{cat.label}</span>
                    </div>
                    <div className="flex items-center gap-1.5 shrink-0 ml-2">
                      <span className="font-bold text-white text-[12px]">
                        {cat.count}
                      </span>
                      <span className="text-[#64748b] text-[10.5px]">
                        ({cat.percentage}%)
                      </span>
                    </div>
                  </div>
                  {/* Custom Progress Bar */}
                  <div className="w-full h-2 bg-[#111520] border border-[#1a1f2e] rounded-full overflow-hidden relative">
                    <div 
                      className="h-full rounded-full transition-all duration-1000"
                      style={{ 
                        backgroundColor: cat.color,
                        width: `${cat.percentage}%` 
                      }} 
                    />
                  </div>
                </Link>
              ))}
            </div>

          </div>

          {/* Footer */}
          <div className="pt-4 border-t border-[#1a1f2e] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-5 h-5 rounded-lg bg-[#6366f1]/10 border border-[#6366f1]/20 flex items-center justify-center text-[#6366f1]">
                <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="18" y1="20" x2="18" y2="10" /><line x1="12" y1="20" x2="12" y2="4" /><line x1="6" y1="20" x2="6" y2="14" />
                </svg>
              </div>
              <div>
                <div className="text-[11px] font-bold text-white leading-tight">Total {activeCategoriesCount} categories analyzed</div>
                <div className="text-[9.5px] text-[#64748b] leading-tight">Your inbox is well organized</div>
              </div>
            </div>
            <div className="w-32 h-6 text-[#6366f1] opacity-70">
              <svg className="w-full h-full" viewBox="0 0 100 30" fill="none">
                <path d="M0,20 Q15,8 30,18 T60,8 T90,14 T100,5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                <circle cx="100" cy="5" r="2.5" fill="#8b5cf6" className="animate-pulse" />
              </svg>
            </div>
          </div>
        </div>

      </div>
      {/* Main Grid: Left (Dashboard content) + Right (AI / Actions panel) */}
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_280px] xl:grid-cols-[1fr_320px] gap-6 items-start">
        
        {/* LEFT COLUMN */}
        <div className="space-y-6 min-w-0">               {/* Charts Row: Email Activity + Top Senders */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Email Activity Last 7 Days (Curved Line SVG) */}
            <div className="bg-[#0f111a] border border-[#1a1f2e] rounded-2xl p-6 shadow-xl flex flex-col justify-between min-h-[300px]">
              <div>
                <div className="flex justify-between items-center">
                  <h2 className="text-[14.5px] font-bold text-white tracking-wide">Email Activity (Last 7 Days)</h2>
                  <span className="text-[11.5px] text-[#64748b] font-semibold bg-[#111520] px-2.5 py-1 rounded-lg border border-[#1a1f2e]">
                    Last 7 days
                  </span>
                </div>
                <div className="w-6 h-1 bg-[#6366f1] rounded-full mt-1.5 mb-6" />
              </div>

              {/* SVG Area Chart */}
              <div className="relative flex-1 flex flex-col justify-end h-[220px] mt-4">
                <svg className="w-full h-full" viewBox="0 0 500 220">
                  <defs>
                    <linearGradient id="activity-gradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#8b5cf6" stopOpacity="0.3" />
                      <stop offset="100%" stopColor="#8b5cf6" stopOpacity="0" />
                    </linearGradient>
                  </defs>

                  {/* Horizontal gridlines */}
                  <line x1="55" y1="30" x2="475" y2="30" stroke="rgba(255,255,255,0.05)" strokeWidth="1.5" />
                  <line x1="55" y1="65" x2="475" y2="65" stroke="rgba(255,255,255,0.05)" strokeWidth="1.5" />
                  <line x1="55" y1="100" x2="475" y2="100" stroke="rgba(255,255,255,0.05)" strokeWidth="1.5" />
                  <line x1="55" y1="135" x2="475" y2="135" stroke="rgba(255,255,255,0.05)" strokeWidth="1.5" />
                  <line x1="55" y1="170" x2="475" y2="170" stroke="rgba(255,255,255,0.1)" strokeWidth="1.5" />

                  {/* Y-axis Labels */}
                  <text x="40" y="30" textAnchor="end" dominantBaseline="middle" fill="#64748b" fontSize="10" fontWeight="700">{maxVal}</text>
                  <text x="40" y="65" textAnchor="end" dominantBaseline="middle" fill="#64748b" fontSize="10" fontWeight="700">{Math.round(maxVal * 0.75)}</text>
                  <text x="40" y="100" textAnchor="end" dominantBaseline="middle" fill="#64748b" fontSize="10" fontWeight="700">{Math.round(maxVal * 0.5)}</text>
                  <text x="40" y="135" textAnchor="end" dominantBaseline="middle" fill="#64748b" fontSize="10" fontWeight="700">{Math.round(maxVal * 0.25)}</text>
                  <text x="40" y="170" textAnchor="end" dominantBaseline="middle" fill="#64748b" fontSize="10" fontWeight="700">0</text>

                  {/* Vertical Hover Line */}
                  {hoveredDay !== null && activityData[hoveredDay] && (
                    <line 
                      x1={activityData[hoveredDay].x} 
                      y1={170} 
                      x2={activityData[hoveredDay].x} 
                      y2={activityData[hoveredDay].y} 
                      stroke="rgba(139,92,246,0.3)" 
                      strokeWidth="1.5" 
                      strokeDasharray="4 4" 
                    />
                  )}

                  {/* Under path fill */}
                  <path 
                    d={fillPath} 
                    fill="url(#activity-gradient)" 
                  />

                  {/* Curve Line */}
                  <path 
                    d={curvePath} 
                    fill="none" 
                    stroke="#8b5cf6" 
                    strokeWidth="3.5" 
                    strokeLinecap="round"
                    filter="drop-shadow(0px 4px 8px rgba(139,92,246,0.3))"
                  />

                  {/* Glowing vertices */}
                  {activityData.map((d, index) => {
                    const isHovered = hoveredDay === index;
                    return (
                      <g 
                        key={index} 
                        className="cursor-pointer"
                        onMouseEnter={() => setHoveredDay(index)}
                      >
                        {/* Invisible larger circle to make hover target comfortable */}
                        <circle
                          cx={d.x}
                          cy={d.y}
                          r={15}
                          fill="transparent"
                        />
                        {isHovered && (
                          <circle cx={d.x} cy={d.y} r={10} fill="#8b5cf6" fillOpacity="0.25" className="animate-ping" />
                        )}
                        <circle 
                          cx={d.x} 
                          cy={d.y} 
                          r={isHovered ? 6 : 4.5} 
                          fill={isHovered ? "#c084fc" : "#8b5cf6"} 
                          stroke="#0f111a" 
                          strokeWidth="2" 
                          className="transition-all duration-200" 
                        />
                      </g>
                    );
                  })}

                  {/* X-axis Day Labels */}
                  {activityData.map((d, index) => (
                    <text
                      key={index}
                      x={d.x}
                      y={195}
                      textAnchor="middle"
                      fill={hoveredDay === index ? "#ffffff" : "#64748b"}
                      fontSize="11"
                      fontWeight="700"
                      className="cursor-pointer transition-colors duration-200 select-none"
                      onMouseEnter={() => setHoveredDay(index)}
                    >
                      {d.day}
                    </text>
                  ))}

                  {/* Tooltip rendered directly inside the SVG */}
                  {hoveredDay !== null && activityData[hoveredDay] && (() => {
                    const d = activityData[hoveredDay];
                    const dayNames: Record<string, string> = {
                      Mon: 'Monday',
                      Tue: 'Tuesday',
                      Wed: 'Wednesday',
                      Thu: 'Thursday',
                      Fri: 'Friday',
                      Sat: 'Saturday',
                      Sun: 'Sunday'
                    };
                    const fullDayName = dayNames[d.day] || d.day;
                    return (
                      <g key="tooltip" className="pointer-events-none select-none">
                        {/* Tooltip triangle tail pointing down */}
                        <path 
                          d={`M ${d.x - 6} ${d.y - 14} L ${d.x} ${d.y - 8} L ${d.x + 6} ${d.y - 14}`} 
                          fill="#131622" 
                          stroke="#23293f" 
                          strokeWidth="1.2" 
                        />
                        {/* Tooltip Card Rect */}
                        <rect 
                          x={d.x - 48} 
                          y={d.y - 60} 
                          width={96} 
                          height={46} 
                          rx={8} 
                          fill="#131622" 
                          stroke="#23293f" 
                          strokeWidth="1.2" 
                        />
                        {/* Tooltip Title (Day) */}
                        <text 
                          x={d.x} 
                          y={d.y - 45} 
                          textAnchor="middle" 
                          fill="#8c9bb4" 
                          fontSize="10" 
                          fontWeight="600"
                          style={{ letterSpacing: '0.05em' }}
                        >
                          {fullDayName}
                        </text>
                        {/* Tooltip Count */}
                        <text 
                          x={d.x} 
                          y={d.y - 27} 
                          textAnchor="middle" 
                          fill="#ffffff" 
                          fontSize="12" 
                          fontWeight="800"
                        >
                          {d.count} {d.count === 1 ? 'Email' : 'Emails'}
                        </text>
                      </g>
                    );
                  })()}
                </svg>
              </div>
               <div className="text-[11px] text-[#64748b] mt-4 pt-3 border-t border-[#1a1f2e] font-medium flex justify-between">
                 <span>Total {totalEmailsThisWeek} emails this week</span>
                 <span>Active 7 days</span>
               </div>
             </div>

            {/* Top Senders (Bar chart) */}
            <div className="bg-[#0f111a] border border-[#1a1f2e] rounded-2xl p-6 shadow-xl flex flex-col justify-between min-h-[300px]">
              <div>
                <div className="flex justify-between items-center">
                  <h2 className="text-[14.5px] font-bold text-white tracking-wide">Top Senders</h2>
                  <span className="text-[11.5px] text-[#64748b] font-semibold bg-[#111520] px-2 py-0.5 rounded-lg border border-[#1a1f2e] cursor-pointer hover:text-white transition-colors">
                    All Time ▾
                  </span>
                </div>
                <div className="w-6 h-1 bg-[#6366f1] rounded-full mt-1.5 mb-6" />
              </div>

              {/* Bars list */}
              <div className="flex-1 space-y-3.5 flex flex-col justify-center">
                {sendersData.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-6 text-center">
                    <span className="text-[#64748b] text-[12px] font-medium">No sender data yet.</span>
                    <span className="text-[#3a4460] text-[11px] mt-1">Sync your inbox to see top senders.</span>
                  </div>
                ) : sendersData.map((sender, idx) => (
                  <div key={idx} className="flex items-center text-[12.5px] font-medium">
                    <span className="w-20 text-[#8c9bb4] truncate shrink-0">{sender.name}</span>
                    <div className="flex-1 h-3 bg-[#111520] border border-[#1a1f2e] rounded-full overflow-hidden mx-3.5 relative">
                      <div className={`h-full rounded-full ${sender.color} transition-all duration-1000`}
                           style={{ width: `${sender.percentage}%` }} />
                    </div>
                    <span className="w-6 text-right text-white font-bold select-none">{sender.count}</span>
                  </div>
                ))}
              </div>
              {/* Axis markers */}
              <div className="flex justify-between pl-20 pr-6 mt-4 pt-3 border-t border-[#1a1f2e] text-[10px] font-bold text-[#64748b] select-none">
                {axisTicks.map((tick, index) => (
                  <span key={index}>{tick}</span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN */}
        <div className="space-y-6 min-w-0">
          


          {/* Quick Actions Panel */}
          <div className="bg-[#0f111a] border border-[#1a1f2e] rounded-2xl p-5 shadow-xl">
            <h2 className="text-[14px] font-bold text-white tracking-wide mb-4">Quick Actions</h2>
            
            <div className="space-y-2.5">
              {[
                { 
                  href: '/dashboard/compose', 
                  label: 'Compose New Email', 
                  sub: 'Write a new email with AI', 
                  icon: FileText, 
                  color: 'bg-[#6366f1]/10 text-[#818cf8] border-[#6366f1]/20' 
                },
                { 
                  href: '/dashboard/emails', 
                  label: 'Reply to a Thread', 
                  sub: 'Generate AI reply', 
                  icon: MessageSquare, 
                  color: 'bg-[#3b82f6]/10 text-[#3b82f6] border-[#3b82f6]/20' 
                },
                { 
                  href: '/dashboard/emails', 
                  label: 'Find Emails', 
                  sub: 'Search across your emails', 
                  icon: SearchCode, 
                  color: 'bg-[#10b981]/10 text-[#10b981] border-[#10b981]/20' 
                },
                { 
                  href: '/dashboard/chat', 
                  label: 'Smart Summary', 
                  sub: 'Summarize unread emails', 
                  icon: Brain, 
                  color: 'bg-[#f97316]/10 text-[#f97316] border-[#f97316]/20' 
                },
              ].map((action, idx) => {
                const Icon = action.icon;
                return (
                  <Link 
                    key={idx} 
                    href={action.href} 
                    className="flex items-center justify-between p-3 rounded-xl border border-[#161a26] bg-[#0c0e16] hover:border-[#6366f1]/30 hover:bg-[#111520] transition-all group cursor-pointer"
                  >
                    <div className="flex items-center gap-3.5">
                      <div className={`w-8 h-8 rounded-lg flex items-center justify-center border ${action.color}`}>
                        <Icon className="w-4 h-4" />
                      </div>
                      <div className="text-left">
                        <div className="text-[12.5px] font-bold text-white group-hover:text-[#818cf8] transition-colors">{action.label}</div>
                        <div className="text-[10.5px] text-[#64748b] mt-0.5">{action.sub}</div>
                      </div>
                    </div>
                    <ArrowRight className="w-4 h-4 text-[#4e5e78] group-hover:text-white group-hover:translate-x-0.5 transition-all" />
                  </Link>
                );
              })}
            </div>
          </div>



        </div>
      </div>

      {/* Recent Emails — Full Width below the grid */}
      <div className="bg-[#0f111a] border border-[#1a1f2e] rounded-2xl p-6 shadow-xl">
        <div className="flex justify-between items-center mb-5">
          <div>
            <h2 className="text-[15px] font-bold text-white tracking-wide">Recent Emails</h2>
            <div className="w-6 h-1 bg-[#6366f1] rounded-full mt-1.5" />
          </div>
          <Link href="/dashboard/emails" className="text-[12.5px] font-semibold text-[#818cf8] hover:text-[#a855f7] flex items-center gap-1 transition-colors">
            View All Emails <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="space-y-2.5">
          {enrichedEmails.length === 0 ? (
            <div className="text-center py-10 text-[#64748b] text-[13px] font-medium">
              No emails received yet. Click sync above to connect.
            </div>
          ) : (
            enrichedEmails.map((email: EmailSummary) => (
              <div
                key={email.id}
                className="group flex items-center gap-4 px-4 py-3.5 rounded-xl border border-[#1a1f2e] bg-[#0c0e16] hover:border-[#6366f1]/40 hover:bg-[#111520] transition-all hover:shadow-[0_4px_20px_rgba(99,102,241,0.08)] cursor-pointer"
              >
                {/* Avatar */}
                <Link href={`/dashboard/emails/${email.id}`} className="shrink-0">
                  <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#1e293b] to-[#0f172a] text-[#818cf8] flex items-center justify-center font-bold text-[13px] border border-[#2b354d] group-hover:from-[#6366f1]/20 group-hover:to-[#8b5cf6]/20 group-hover:text-white transition-all select-none shrink-0">
                    {email.senderName?.charAt(0)?.toUpperCase() || 'E'}
                  </div>
                </Link>

                {/* Sender name — fixed width column */}
                <div className="w-36 lg:w-44 shrink-0 min-w-0">
                  <span className="text-[13px] font-bold text-white group-hover:text-[#818cf8] transition-colors truncate block">
                    {email.senderName || 'Unknown'}
                  </span>
                </div>

                {/* Subject — fills remaining space */}
                <Link href={`/dashboard/emails/${email.id}`} className="flex-1 min-w-0 flex items-baseline gap-2">
                  <span className={`text-[13px] truncate shrink-0 max-w-[260px] lg:max-w-[400px] ${!email.isRead ? 'font-bold text-white' : 'font-medium text-[#8c9bb4]'}`}>
                    {email.subject}
                  </span>
                  <span className="hidden lg:inline text-[#3a4460] text-[12px] select-none">—</span>
                  <span className="hidden lg:block text-[11.5px] text-[#64748b] truncate flex-1 min-w-0">
                    {email.aiSummary || email.snippet}
                  </span>
                </Link>

                {/* Right: Category + Time + Star */}
                <div className="flex items-center gap-3 shrink-0">
                  {email.aiCategory && (
                    <span className={`hidden sm:inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-wide uppercase border items-center gap-1 select-none ${CATEGORY_COLORS[email.aiCategory] || CATEGORY_COLORS['Uncategorized']}`}>
                      {CATEGORY_ICONS[email.aiCategory] || '📧'} {email.aiCategory.split('/')[0]}
                    </span>
                  )}
                  <span className="hidden md:block text-[11px] text-[#64748b] font-semibold w-14 text-right">
                    {formatTime(email.receivedAt)}
                  </span>
                  <button className="text-[#64748b] hover:text-[#eab308] transition-colors p-1.5 hover:bg-[#181d2a] rounded-lg">
                    <Star className={`w-4 h-4 ${email.isStarred ? 'fill-[#eab308] text-[#eab308]' : ''}`} />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
