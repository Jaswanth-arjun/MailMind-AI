'use client';

import Link from 'next/link';
import { MouseEvent, useEffect, useMemo, useRef, useState } from 'react';
import {
  ArrowRight,
  BarChart3,
  BotMessageSquare,
  CheckCircle2,
  Inbox,
  Link2,
  Mail,
  MessageSquareText,
  Play,
  Search,
  Sparkles,
  WandSparkles,
} from 'lucide-react';
import {
  motion,
  useMotionTemplate,
  useMotionValue,
  useReducedMotion,
  useSpring,
  useTransform,
} from 'framer-motion';

const emailRows = [
  { from: 'Maya at Linear', subject: 'Launch review notes', meta: '8 min ago', category: 'Work', color: 'bg-blue-50 text-blue-700' },
  { from: 'Stripe Billing', subject: 'Invoice paid successfully', meta: '22 min ago', category: 'Finance', color: 'bg-violet-50 text-violet-700' },
  { from: 'Google Security', subject: 'New OAuth scope approved', meta: '1 hr ago', category: 'Security', color: 'bg-emerald-50 text-emerald-700' },
  { from: 'Product Updates', subject: 'Weekly SaaS digest', meta: 'Yesterday', category: 'News', color: 'bg-sky-50 text-sky-700' },
];

const particles = Array.from({ length: 22 }, (_, index) => ({
  id: index,
  left: `${8 + ((index * 37) % 84)}%`,
  top: `${10 + ((index * 53) % 78)}%`,
  size: 2 + (index % 3),
  opacity: 0.16 + (index % 4) * 0.04,
}));

function GoogleIcon({ className = 'h-5 w-5' }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.27-4.74 3.27-8.1Z" />
      <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84A11 11 0 0 0 12 23Z" />
      <path fill="#FBBC05" d="M5.84 14.09A6.6 6.6 0 0 1 5.49 12c0-.73.13-1.43.35-2.09V7.07H2.18A11 11 0 0 0 1 12c0 1.78.43 3.45 1.18 4.93l3.66-2.84Z" />
      <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15A10.96 10.96 0 0 0 12 1 11 11 0 0 0 2.18 7.07l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38Z" />
    </svg>
  );
}

function MagneticButton({
  children,
  onClick,
  variant = 'primary',
  disabled,
}: {
  children: React.ReactNode;
  onClick?: () => void;
  variant?: 'primary' | 'secondary';
  disabled?: boolean;
}) {
  const x = useMotionValue(0);
  const y = useMotionValue(0);
  const springX = useSpring(x, { stiffness: 260, damping: 18, mass: 0.35 });
  const springY = useSpring(y, { stiffness: 260, damping: 18, mass: 0.35 });

  const handleMove = (event: MouseEvent<HTMLButtonElement>) => {
    const rect = event.currentTarget.getBoundingClientRect();
    x.set((event.clientX - rect.left - rect.width / 2) * 0.18);
    y.set((event.clientY - rect.top - rect.height / 2) * 0.22);
  };

  return (
    <motion.button
      type="button"
      onClick={onClick}
      disabled={disabled}
      onMouseMove={handleMove}
      onMouseLeave={() => {
        x.set(0);
        y.set(0);
      }}
      whileHover={{ scale: 1.035, y: -2 }}
      whileTap={{ scale: 0.985 }}
      style={{ x: springX, y: springY }}
      className={[
        'group relative inline-flex h-12 items-center justify-center gap-2 overflow-hidden rounded-full px-6 text-sm font-semibold transition-shadow disabled:cursor-wait disabled:opacity-70',
        variant === 'primary'
          ? 'bg-slate-950 text-white shadow-[0_18px_45px_rgba(45,63,130,0.28)]'
          : 'border border-slate-200 bg-white/70 text-slate-800 shadow-[0_16px_40px_rgba(78,90,120,0.12)] backdrop-blur-xl',
      ].join(' ')}
    >
      <span className="absolute inset-0 opacity-0 transition-opacity duration-300 group-hover:opacity-100" style={{ background: variant === 'primary' ? 'linear-gradient(135deg, #1d4ed8, #6d28d9)' : 'linear-gradient(135deg, rgba(255,255,255,.85), rgba(239,246,255,.75))' }} />
      <span className="relative z-10 inline-flex items-center gap-2">{children}</span>
    </motion.button>
  );
}

function FloatingCard({
  children,
  className,
  depth,
  x,
  y,
  blur = 0,
}: {
  children: React.ReactNode;
  className: string;
  depth: number;
  x: ReturnType<typeof useSpring>;
  y: ReturnType<typeof useSpring>;
  blur?: number;
}) {
  const parallaxX = useTransform(x, [-1, 1], [-depth * 14, depth * 14]);
  const parallaxY = useTransform(y, [-1, 1], [-depth * 10, depth * 10]);
  const scale = useTransform(x, [-1, 0, 1], [0.985, 1.015, 0.985]);

  return (
    <motion.div
      whileHover={{ y: -8, scale: 1.035 }}
      style={{
        x: parallaxX,
        y: parallaxY,
        scale,
        transformStyle: 'preserve-3d',
        filter: blur ? `blur(${blur}px)` : undefined,
      }}
      className={`absolute rounded-2xl border border-white/70 bg-white/78 p-4 shadow-[0_28px_70px_rgba(45,62,105,0.18)] backdrop-blur-2xl transition-colors hover:border-blue-200 ${className}`}
    >
      {children}
    </motion.div>
  );
}

function ConnectionLines() {
  return (
    <svg className="absolute inset-0 h-full w-full overflow-visible" viewBox="0 0 720 560" fill="none" aria-hidden="true">
      <motion.path
        d="M362 116 C470 78 532 110 586 178"
        stroke="url(#lineGradient)"
        strokeWidth="1.2"
        strokeDasharray="7 9"
        initial={{ pathLength: 0.25, opacity: 0.2 }}
        animate={{ pathLength: [0.35, 1, 0.35], opacity: [0.25, 0.65, 0.25] }}
        transition={{ duration: 5.4, repeat: Infinity, ease: 'easeInOut' }}
      />
      <motion.path
        d="M354 344 C475 368 520 312 610 292"
        stroke="url(#lineGradient)"
        strokeWidth="1.2"
        strokeDasharray="7 9"
        initial={{ pathLength: 0.2, opacity: 0.2 }}
        animate={{ pathLength: [0.25, 1, 0.25], opacity: [0.2, 0.6, 0.2] }}
        transition={{ duration: 6.2, repeat: Infinity, ease: 'easeInOut', delay: 0.8 }}
      />
      <motion.path
        d="M242 146 C162 172 138 238 98 300"
        stroke="url(#lineGradient)"
        strokeWidth="1.2"
        strokeDasharray="7 9"
        initial={{ pathLength: 0.2, opacity: 0.2 }}
        animate={{ pathLength: [0.2, 0.95, 0.2], opacity: [0.18, 0.58, 0.18] }}
        transition={{ duration: 5.8, repeat: Infinity, ease: 'easeInOut', delay: 1.1 }}
      />
      <defs>
        <linearGradient id="lineGradient" x1="80" x2="640" y1="70" y2="390" gradientUnits="userSpaceOnUse">
          <stop stopColor="#60A5FA" stopOpacity="0" />
          <stop offset="0.48" stopColor="#7C3AED" stopOpacity="0.75" />
          <stop offset="1" stopColor="#38BDF8" stopOpacity="0" />
        </linearGradient>
      </defs>
    </svg>
  );
}

function DashboardPanel() {
  return (
    <div className="h-full overflow-hidden rounded-[1.55rem] border border-white/80 bg-white/88 shadow-[0_42px_120px_rgba(30,41,80,0.22)] backdrop-blur-xl">
      <div className="flex items-center justify-between border-b border-slate-200/70 bg-white/80 px-5 py-3">
        <div className="flex items-center gap-2">
          <span className="h-3 w-3 rounded-full bg-red-400" />
          <span className="h-3 w-3 rounded-full bg-amber-300" />
          <span className="h-3 w-3 rounded-full bg-emerald-400" />
        </div>
        <div className="flex w-64 items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-[11px] font-medium text-slate-500">
          <Search className="h-3.5 w-3.5" />
          Ask about Q3 renewals
        </div>
        <div className="h-7 w-7 rounded-full bg-gradient-to-br from-blue-600 to-violet-600" />
      </div>

      <div className="grid h-[410px] grid-cols-[150px_1fr] bg-slate-50/60">
        <aside className="border-r border-slate-200/70 bg-white/72 p-4">
          <div className="mb-5 flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-slate-950 text-sm font-black text-white">M</div>
            <div className="text-[13px] font-bold text-slate-950">MailMind AI</div>
          </div>
          {['Inbox', 'AI Chat', 'Summaries', 'Categories', 'Drafts'].map((item, index) => (
            <div key={item} className={`mb-1.5 flex items-center justify-between rounded-xl px-3 py-2 text-[11px] font-semibold ${index === 0 ? 'bg-blue-50 text-blue-700' : 'text-slate-500'}`}>
              <span>{item}</span>
              {index === 0 && <span className="rounded-full bg-blue-100 px-1.5 text-[10px]">128</span>}
            </div>
          ))}
          <div className="mt-8 rounded-2xl border border-blue-100 bg-gradient-to-br from-blue-50 to-violet-50 p-3">
            <div className="mb-2 flex items-center gap-1.5 text-[11px] font-bold text-slate-900">
              <Sparkles className="h-3.5 w-3.5 text-violet-600" />
              Smart Sync
            </div>
            <div className="h-1.5 overflow-hidden rounded-full bg-white">
              <motion.div className="h-full rounded-full bg-gradient-to-r from-blue-500 to-violet-500" animate={{ width: ['42%', '76%', '58%'] }} transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }} />
            </div>
          </div>
        </aside>

        <main className="p-4">
          <div className="mb-3 flex items-center justify-between">
            <div>
              <div className="text-[11px] font-semibold uppercase text-slate-400">Unified Inbox</div>
              <h3 className="text-lg font-bold text-slate-950">Priority threads</h3>
            </div>
            <div className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-[11px] font-bold text-emerald-700">Gmail connected</div>
          </div>

          <div className="space-y-2">
            {emailRows.map((row) => (
              <div key={row.subject} className="flex items-center justify-between rounded-2xl border border-slate-200/70 bg-white/82 p-3 shadow-sm">
                <div className="flex min-w-0 items-center gap-3">
                  <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-100 text-xs font-bold text-slate-600">{row.from[0]}</div>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="truncate text-[12px] font-bold text-slate-900">{row.from}</span>
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${row.color}`}>{row.category}</span>
                    </div>
                    <div className="truncate text-[11px] font-medium text-slate-500">{row.subject}</div>
                  </div>
                </div>
                <span className="shrink-0 text-[10px] font-semibold text-slate-400">{row.meta}</span>
              </div>
            ))}
          </div>

          <div className="mt-3 grid grid-cols-3 gap-2">
            {[
              ['12', 'Unread summarized'],
              ['4', 'Replies drafted'],
              ['92%', 'Search confidence'],
            ].map(([value, label]) => (
              <div key={label} className="rounded-2xl border border-white bg-white/75 p-3 shadow-sm">
                <div className="text-xl font-bold text-slate-950">{value}</div>
                <div className="text-[10px] font-semibold text-slate-500">{label}</div>
              </div>
            ))}
          </div>
        </main>
      </div>
    </div>
  );
}

function WorkspaceScene({
  sceneX,
  sceneY,
  reduced,
}: {
  sceneX: ReturnType<typeof useSpring>;
  sceneY: ReturnType<typeof useSpring>;
  reduced: boolean;
}) {
  const rotateY = useTransform(sceneX, [-1, 1], reduced ? [0, 0] : [-12, 12]);
  const rotateX = useTransform(sceneY, [-1, 1], reduced ? [0, 0] : [9, -9]);
  const shadowX = useTransform(sceneX, [-1, 1], [-28, 28]);
  const shadowY = useTransform(sceneY, [-1, 1], [38, 18]);
  const dashboardShadow = useMotionTemplate`${shadowX}px ${shadowY}px 90px rgba(45, 58, 108, 0.22)`;
  const backX = useTransform(sceneX, [-1, 1], [14, -14]);
  const backY = useTransform(sceneY, [-1, 1], [10, -10]);

  return (
    <div className="relative mx-auto h-[600px] w-full max-w-[780px] [perspective:1500px] max-lg:h-[540px] max-sm:h-auto max-sm:min-h-[720px]">
      <motion.div
        className="absolute inset-0 rounded-[3rem] bg-[radial-gradient(circle_at_50%_45%,rgba(99,102,241,0.18),transparent_48%)] blur-2xl"
        style={{ x: backX, y: backY }}
      />
      <motion.div
        className="absolute inset-0"
        style={{
          rotateX,
          rotateY,
          transformStyle: 'preserve-3d',
          transformPerspective: 1400,
        }}
      >
        <ConnectionLines />

        <motion.div
          className="absolute left-[17%] top-[14%] h-[438px] w-[494px] max-sm:left-0 max-sm:top-20 max-sm:w-full"
          style={{
            transform: 'translateZ(40px)',
            boxShadow: dashboardShadow,
            transformStyle: 'preserve-3d',
          }}
        >
          <DashboardPanel />
        </motion.div>

        <FloatingCard x={sceneX} y={sceneY} depth={0.45} blur={0.25} className="left-[-2%] top-[5%] w-52 max-sm:left-2 max-sm:top-0" >
          <div style={{ transform: 'translateZ(20px)' }}>
            <div className="mb-2 flex items-center gap-2 text-[12px] font-bold text-slate-900">
              <CheckCircle2 className="h-4 w-4 text-emerald-500" />
              Gmail connected
            </div>
            <p className="text-[11px] leading-5 text-slate-500">Syncing 18,420 indexed messages with OAuth 2.0.</p>
          </div>
        </FloatingCard>

        <FloatingCard x={sceneX} y={sceneY} depth={1} className="right-[-4%] top-[9%] w-64 max-sm:right-0 max-sm:top-20">
          <div style={{ transform: 'translateZ(120px)' }}>
            <div className="mb-3 flex items-center justify-between">
              <div className="flex items-center gap-2 text-[12px] font-bold text-slate-950">
                <WandSparkles className="h-4 w-4 text-violet-600" />
                AI summary
              </div>
              <span className="rounded-full bg-violet-50 px-2 py-1 text-[10px] font-bold text-violet-700">3 sources</span>
            </div>
            <p className="text-[12px] leading-5 text-slate-600">Q3 launch needs legal review, pricing sign-off, and one reply to Maya before Friday.</p>
          </div>
        </FloatingCard>

        <FloatingCard x={sceneX} y={sceneY} depth={0.7} className="right-[-2%] top-[40%] w-60 max-sm:right-0 max-sm:top-[280px]">
          <div style={{ transform: 'translateZ(70px)' }}>
            <div className="mb-3 flex items-center gap-2 text-[12px] font-bold text-slate-950">
              <BotMessageSquare className="h-4 w-4 text-blue-600" />
              Inbox chat
            </div>
            <div className="rounded-2xl bg-slate-50 p-3 text-[11px] font-medium leading-5 text-slate-600">"Which renewal threads mention security review?"</div>
          </div>
        </FloatingCard>

        <FloatingCard x={sceneX} y={sceneY} depth={0.62} className="left-[-4%] top-[54%] w-56 max-sm:left-0 max-sm:top-[420px]">
          <div style={{ transform: 'translateZ(70px)' }}>
            <div className="mb-3 flex items-center gap-2 text-[12px] font-bold text-slate-950">
              <BarChart3 className="h-4 w-4 text-sky-600" />
              Categories
            </div>
            <div className="space-y-2">
              {['Work', 'Finance', 'News'].map((item, index) => (
                <div key={item} className="flex items-center gap-2">
                  <span className="w-14 text-[10px] font-bold text-slate-500">{item}</span>
                  <span className="h-2 flex-1 overflow-hidden rounded-full bg-slate-100">
                    <motion.span className="block h-full rounded-full bg-gradient-to-r from-blue-500 to-violet-500" animate={{ width: [`${36 + index * 16}%`, `${62 - index * 5}%`, `${42 + index * 14}%`] }} transition={{ duration: 4 + index, repeat: Infinity, ease: 'easeInOut' }} />
                  </span>
                </div>
              ))}
            </div>
          </div>
        </FloatingCard>

        <FloatingCard x={sceneX} y={sceneY} depth={0.92} className="bottom-[1%] right-[7%] w-64 max-sm:right-0 max-sm:bottom-6">
          <div style={{ transform: 'translateZ(120px)' }}>
            <div className="mb-3 flex items-center gap-2 text-[12px] font-bold text-slate-950">
              <MessageSquareText className="h-4 w-4 text-violet-600" />
              Thread-aware reply
            </div>
            <p className="mb-3 text-[11px] leading-5 text-slate-500">Drafted from the latest 9-message thread.</p>
            <div className="rounded-2xl border border-blue-100 bg-blue-50/70 p-3 text-[11px] font-semibold text-blue-700">Send revised timeline and confirm Friday review.</div>
          </div>
        </FloatingCard>

        <FloatingCard x={sceneX} y={sceneY} depth={0.35} blur={0.2} className="bottom-[9%] left-[14%] w-48 max-sm:left-4 max-sm:bottom-[150px]">
          <div style={{ transform: 'translateZ(20px)' }}>
            <div className="mb-2 flex items-center gap-2 text-[12px] font-bold text-slate-950">
              <Link2 className="h-4 w-4 text-slate-600" />
              Citations
            </div>
            <div className="space-y-1.5 text-[10px] font-semibold text-slate-500">
              <div className="rounded-full bg-slate-50 px-2 py-1">Maya - Launch notes</div>
              <div className="rounded-full bg-slate-50 px-2 py-1">Legal - Approval thread</div>
            </div>
          </div>
        </FloatingCard>

        {[
          ['Contract.pdf received', 'left-[2%] top-[33%]', 'translateZ(120px)'],
          ['Reply ready', 'right-[-1%] bottom-[29%]', 'translateZ(70px)'],
          ['7 priority emails', 'left-[40%] top-[2%]', 'translateZ(120px)'],
        ].map(([label, position, z]) => (
          <motion.div
            key={label}
            className={`absolute ${position} rounded-full border border-white/80 bg-white/82 px-3 py-2 text-[11px] font-bold text-slate-700 shadow-[0_18px_44px_rgba(45,62,105,0.16)] backdrop-blur-xl`}
            style={{ transform: z }}
            animate={{ y: [0, -8, 0] }}
            transition={{ duration: 4.5, repeat: Infinity, ease: 'easeInOut' }}
            whileHover={{ y: -10, scale: 1.05 }}
          >
            {label}
          </motion.div>
        ))}
      </motion.div>
    </div>
  );
}

export default function LandingPage() {
  const heroRef = useRef<HTMLElement>(null);
  const [connecting, setConnecting] = useState(false);
  const [hasToken, setHasToken] = useState(false);
  const reducedMotion = useReducedMotion();
  const rawX = useMotionValue(0);
  const rawY = useMotionValue(0);
  const cursorX = useMotionValue(50);
  const cursorY = useMotionValue(50);
  const sceneX = useSpring(rawX, { stiffness: 85, damping: 22, mass: 0.6 });
  const sceneY = useSpring(rawY, { stiffness: 85, damping: 22, mass: 0.6 });
  const glowX = useSpring(cursorX, { stiffness: 140, damping: 24 });
  const glowY = useSpring(cursorY, { stiffness: 140, damping: 24 });
  const background = useMotionTemplate`
    radial-gradient(circle at ${glowX}% ${glowY}%, rgba(96,165,250,0.24), transparent 34%),
    radial-gradient(circle at ${useTransform(glowX, (v) => 100 - v)}% ${useTransform(glowY, (v) => 100 - v)}%, rgba(124,58,237,0.16), transparent 38%),
    linear-gradient(135deg, #ffffff 0%, #f8fbff 46%, #f6f4ff 100%)
  `;
  const particleX = useTransform(sceneX, [-1, 1], [20, -20]);
  const particleY = useTransform(sceneY, [-1, 1], [14, -14]);

  const isReduced = useMemo(() => Boolean(reducedMotion), [reducedMotion]);

  useEffect(() => {
    setHasToken(Boolean(localStorage.getItem('mailmind_token')));
  }, []);

  const handleHeroMove = (event: MouseEvent<HTMLElement>) => {
    if (isReduced || window.innerWidth < 768) return;

    const rect = event.currentTarget.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width;
    const y = (event.clientY - rect.top) / rect.height;
    rawX.set((x - 0.5) * 2);
    rawY.set((y - 0.5) * 2);
    cursorX.set(x * 100);
    cursorY.set(y * 100);
  };

  const resetHero = () => {
    rawX.set(0);
    rawY.set(0);
    cursorX.set(52);
    cursorY.set(42);
  };

  const handleConnectGmail = async () => {
    setConnecting(true);
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
      const res = await fetch(`${apiUrl}/api/gmail/auth-url`);
      if (!res.ok) throw new Error(`Failed to fetch auth URL: ${res.statusText}`);
      const data = await res.json();
      if (data.authUrl) {
        window.location.href = data.authUrl;
      } else {
        alert('Failed to retrieve authentication URL from backend.');
      }
    } catch (err) {
      console.error(err);
      alert('Error contacting the backend auth service. Make sure the backend is running.');
    } finally {
      setConnecting(false);
    }
  };

  return (
    <motion.main
      ref={heroRef}
      onMouseMove={handleHeroMove}
      onMouseLeave={resetHero}
      style={{ background }}
      className="relative min-h-screen overflow-hidden text-slate-950 selection:bg-blue-200/60"
    >
      <motion.div className="pointer-events-none absolute inset-0 z-0" style={{ x: particleX, y: particleY }}>
        {particles.map((particle) => (
          <motion.span
            key={particle.id}
            className="absolute rounded-full bg-slate-500"
            style={{ left: particle.left, top: particle.top, width: particle.size, height: particle.size, opacity: particle.opacity }}
            animate={isReduced ? undefined : { y: [0, -12, 0], opacity: [particle.opacity, particle.opacity * 1.8, particle.opacity] }}
            transition={{ duration: 8 + (particle.id % 6), repeat: Infinity, ease: 'easeInOut', delay: particle.id * 0.15 }}
          />
        ))}
      </motion.div>

      <motion.div
        className="pointer-events-none absolute h-72 w-72 rounded-full bg-blue-400/18 blur-3xl"
        style={{ left: useMotionTemplate`calc(${glowX}% - 9rem)`, top: useMotionTemplate`calc(${glowY}% - 9rem)` }}
      />

      <nav className="relative z-20 mx-auto flex max-w-7xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-950 text-lg font-black text-white shadow-[0_18px_40px_rgba(15,23,42,0.2)]">M</div>
          <span className="text-lg font-bold tracking-tight">MailMind AI</span>
        </div>
        <div className="hidden items-center gap-3 md:flex">
          {hasToken && (
            <Link href="/dashboard" className="rounded-full border border-slate-200 bg-white/70 px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm backdrop-blur-xl transition hover:-translate-y-0.5 hover:border-blue-200">
              Dashboard
            </Link>
          )}
          <button onClick={handleConnectGmail} className="rounded-full bg-slate-950 px-4 py-2 text-sm font-semibold text-white shadow-[0_14px_32px_rgba(15,23,42,0.18)] transition hover:-translate-y-0.5">
            Connect Gmail
          </button>
        </div>
      </nav>

      <section className="relative z-10 mx-auto grid min-h-[calc(100vh-88px)] max-w-7xl grid-cols-1 items-center gap-10 px-6 pb-16 pt-6 lg:grid-cols-[0.9fr_1.1fr]">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
          className="max-w-2xl"
        >
          <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-blue-200 bg-white/70 px-3.5 py-2 text-xs font-bold text-blue-700 shadow-sm backdrop-blur-xl">
            <Sparkles className="h-3.5 w-3.5 text-violet-600" />
            AI-Powered Gmail Intelligence
          </div>

          <h1 className="max-w-3xl text-5xl font-semibold leading-[1.02] tracking-normal text-slate-950 md:text-6xl lg:text-7xl">
            Turn Your Gmail Into An AI Intelligence Workspace
          </h1>

          <p className="mt-6 max-w-xl text-lg leading-8 text-slate-600">
            Connect Gmail, summarize threads, generate replies, and chat with your inbox using AI-powered search and reasoning.
          </p>

          <div className="mt-9 flex flex-col gap-3 sm:flex-row">
            <MagneticButton onClick={handleConnectGmail} disabled={connecting}>
              <GoogleIcon />
              {connecting ? 'Connecting...' : 'Connect Gmail'}
              <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
            </MagneticButton>
            <MagneticButton variant="secondary">
              <Play className="h-4 w-4 fill-slate-800" />
              View Demo
            </MagneticButton>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, scale: 0.96, y: 28 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.12, ease: [0.16, 1, 0.3, 1] }}
          className="relative"
        >
          <WorkspaceScene sceneX={sceneX} sceneY={sceneY} reduced={isReduced} />
        </motion.div>
      </section>
    </motion.main>
  );
}
