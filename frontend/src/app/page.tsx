'use client';

import Link from 'next/link';
import { useState, useEffect } from 'react';
import { Sparkles, Mail, Lock, ShieldCheck, Play, Cpu, Brain, Folder, Send, Search } from 'lucide-react';

export default function LandingPage() {
  const [mounted, setMounted] = useState(false);
  const [connecting, setConnecting] = useState(false);
  const [hasToken, setHasToken] = useState(false);

  useEffect(() => {
    setMounted(true);
    setHasToken(!!localStorage.getItem('mailmind_token'));
  }, []);

  const handleConnectGmail = async () => {
    setConnecting(true);
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
      const res = await fetch(`${apiUrl}/api/gmail/auth-url`);
      if (!res.ok) {
        throw new Error(`Failed to fetch auth URL: ${res.statusText}`);
      }
      const data = await res.json();
      if (data.authUrl) {
        window.location.href = data.authUrl;
      } else {
        alert("Failed to retrieve authentication URL from backend.");
      }
    } catch (err) {
      console.error(err);
      alert("Error contacting the backend auth service. Make sure the backend is running.");
    } finally {
      setConnecting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#080710] text-[#f1f5f9] font-sans selection:bg-indigo-500/30 selection:text-white relative overflow-hidden">
      
      {/* Ambient Glowing Blobs */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div className="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] rounded-full bg-indigo-500/10 blur-[130px] animate-pulse-slow" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[600px] h-[600px] rounded-full bg-cyan-500/10 blur-[130px] animate-pulse-slow" style={{ animationDelay: '3s' }} />
        <div className="absolute top-[40%] left-[50%] -translate-x-1/2 -translate-y-1/2 w-[900px] h-[900px] rounded-full bg-purple-500/5 blur-[150px]" />
        <div className="absolute top-[10%] right-[15%] w-[300px] h-[300px] rounded-full bg-violet-600/10 blur-[90px] animate-glow-pulse" />
      </div>

      {/* Nav */}
      <nav className="fixed top-0 left-0 right-0 z-50 border-b border-white/5 bg-[#080710]/70 backdrop-blur-xl px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          
          {/* Left Logo */}
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 via-purple-500 to-cyan-500 flex items-center justify-center text-white font-extrabold text-lg shadow-[0_0_15px_rgba(99,102,241,0.4)]">
              M
            </div>
            <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-indigo-200 bg-clip-text text-transparent">
              MailMind <span className="text-indigo-400">AI</span>
            </span>
          </div>

          {/* Center Nav Links */}
          <div className="hidden md:flex items-center gap-8 text-sm font-medium text-slate-300">
            <Link href="#features" className="hover:text-white transition-colors">Features</Link>
            <Link href="#how-it-works" className="hover:text-white transition-colors">How it Works</Link>
            <Link href="#security" className="hover:text-white transition-colors">Security</Link>
            <Link href="#pricing" className="hover:text-white transition-colors">Pricing</Link>
            <Link href="#docs" className="hover:text-white transition-colors">Docs</Link>
            <div className="flex items-center gap-1.5">
              <Link href="#changelog" className="hover:text-white transition-colors">Changelog</Link>
              <span className="px-1.5 py-0.5 text-[10px] font-bold text-pink-400 bg-pink-500/10 border border-pink-500/20 rounded-md">New</span>
            </div>
          </div>

          {/* Right Tech / Connect Button */}
          <div className="flex items-center gap-4">
            
            {/* Powered by Gemini & NIM badge */}
            <div className="hidden lg:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-900/80 border border-white/5 text-[11px] font-semibold text-slate-400">
              <span>Powered by</span>
              <span className="flex items-center gap-1 text-indigo-300">
                <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
                Gemini
              </span>
              <span className="text-slate-600">|</span>
              <span className="flex items-center gap-1 text-cyan-300">
                <Cpu className="w-3.5 h-3.5 text-cyan-400" />
                NVIDIA NIM
              </span>
            </div>
            
            <button 
              onClick={handleConnectGmail} 
              disabled={connecting} 
              className="relative group px-4 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 shadow-[0_4px_12px_rgba(99,102,241,0.25)] transition-all duration-300 flex items-center gap-2 cursor-pointer"
            >
              <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" />
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
              </svg>
              <span>{connecting ? 'Connecting...' : 'Connect Gmail'}</span>
            </button>
          </div>
        </div>
      </nav>

      {/* Main Hero & Content */}
      <main className={`max-w-7xl mx-auto pt-36 pb-20 px-6 z-10 relative transition-all duration-1000 ${mounted ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
        <div className="grid lg:grid-cols-12 gap-12 items-center">
          
          {/* Left Column - Copy & CTA */}
          <div className="lg:col-span-5 flex flex-col gap-6 text-left">
            <div className="inline-flex items-center gap-2 self-start px-3.5 py-1.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-xs font-semibold text-indigo-300 animate-float">
              <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
              <span>AI-Powered Gmail Intelligence</span>
            </div>

            <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold leading-[1.15] tracking-tight text-white">
              Your Inbox, <br />
              <span className="bg-gradient-to-r from-blue-400 via-indigo-400 to-purple-400 bg-clip-text text-transparent">
                Supercharged
              </span> <br />
              with AI <span className="inline-block animate-bounce" style={{ animationDuration: '3s' }}>🚀</span>
            </h1>

            <p className="text-base md:text-lg text-slate-400 leading-relaxed max-w-lg">
              MailMind AI connects to your Gmail, understands your emails, summarizes what matters, generates smart replies, and helps you get things done—faster.
            </p>

            {/* Buttons */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 mt-2">
              <button 
                onClick={handleConnectGmail} 
                disabled={connecting}
                className="flex items-center justify-center gap-3 bg-gradient-to-r from-indigo-500 to-blue-600 hover:from-indigo-600 hover:to-blue-700 text-white font-semibold text-base py-3.5 px-7 rounded-xl shadow-[0_4px_20px_rgba(99,102,241,0.4)] transition-all duration-300 cursor-pointer"
              >
                <svg className="w-5 h-5 text-white" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" />
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                </svg>
                <span>{connecting ? 'Connecting...' : 'Connect with Google'}</span>
              </button>
              
              {hasToken && (
                <Link 
                  href="/dashboard" 
                  className="flex items-center justify-center gap-2 bg-slate-900/60 hover:bg-slate-900 border border-white/10 hover:border-white/20 text-slate-300 hover:text-white font-semibold text-base py-3.5 px-7 rounded-xl transition-all duration-300"
                >
                  <Play className="w-4 h-4 fill-slate-300 text-slate-300" />
                  <span>Go to Dashboard</span>
                </Link>
              )}
            </div>

            {/* Bullet Features */}
            <div className="flex flex-col gap-3.5 mt-4 text-sm font-medium text-slate-400">
              <div className="flex items-center gap-2.5">
                <div className="w-5 h-5 rounded-md bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
                  <ShieldCheck className="w-3.5 h-3.5 text-indigo-400" />
                </div>
                <span><strong className="text-slate-300">Secure by design</strong> – Your data stays private</span>
              </div>
              <div className="flex items-center gap-2.5">
                <div className="w-5 h-5 rounded-md bg-purple-500/10 border border-purple-500/20 flex items-center justify-center">
                  <Sparkles className="w-3.5 h-3.5 text-purple-400" />
                </div>
                <span><strong className="text-slate-300">AI that understands</strong> – Powered by Gemini & NIM</span>
              </div>
              <div className="flex items-center gap-2.5">
                <div className="w-5 h-5 rounded-md bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center">
                  <Lock className="w-3.5 h-3.5 text-cyan-400" />
                </div>
                <span><strong className="text-slate-300">You're in control</strong> – Only you have access</span>
              </div>
            </div>
          </div>

          {/* Right Column - Beautiful 3D Perspective Dashboard */}
          <div className="lg:col-span-7 relative flex justify-center items-center perspective-1000 select-none">
            
            {/* Glow highlight behind mockup */}
            <div className="absolute w-[80%] h-[85%] bg-gradient-to-tr from-indigo-500/20 via-purple-500/20 to-cyan-500/20 blur-[60px] opacity-70 z-0 animate-pulse-slow" />
            
            {/* Floating 3D envelope icon */}
            <div className="absolute top-[10%] right-[-6%] z-20 animate-float-envelope pointer-events-none hidden md:block">
              <div className="relative p-4 rounded-2xl bg-gradient-to-br from-indigo-600/90 to-purple-600/90 border border-white/10 shadow-[0_15px_30px_rgba(99,102,241,0.4)] glow backdrop-blur-md">
                <Mail className="w-8 h-8 text-white" />
              </div>
            </div>
            
            {/* Floating 3D sparkle icon */}
            <div className="absolute bottom-[20%] right-[-10%] z-20 animate-float-star pointer-events-none hidden md:block" style={{ animationDelay: '1.5s' }}>
              <div className="relative p-3.5 rounded-full bg-[#0d0a1b]/95 border border-cyan-500/30 shadow-[0_10px_25px_rgba(6,182,212,0.3)] backdrop-blur-md flex items-center justify-center">
                <Sparkles className="w-6 h-6 text-cyan-400" />
              </div>
            </div>

            {/* Main Dashboard Mockup container */}
            <div className="dashboard-mockup glow-border-purple w-full max-w-[660px] rounded-2xl bg-[#090810]/95 border border-white/5 overflow-hidden z-10 flex flex-col font-sans text-xs">
              
              {/* Top Navbar in mockup */}
              <div className="flex items-center justify-between px-4 py-2.5 border-b border-white/5 bg-[#0b0a15]/90">
                <div className="flex items-center gap-1.5">
                  {/* Window controls */}
                  <div className="w-2.5 h-2.5 rounded-full bg-red-500/70" />
                  <div className="w-2.5 h-2.5 rounded-full bg-yellow-500/70" />
                  <div className="w-2.5 h-2.5 rounded-full bg-green-500/70" />
                </div>
                {/* Search bar mockup */}
                <div className="flex-1 max-w-[280px] mx-4 relative">
                  <input 
                    type="text" 
                    placeholder="Search emails, senders, topics..." 
                    disabled
                    className="w-full bg-white/5 border border-white/5 rounded-lg pl-8 pr-8 py-1 text-[10px] text-slate-400 placeholder-slate-500 focus:outline-none"
                  />
                  <svg className="w-3 h-3 text-slate-500 absolute left-2.5 top-1.5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                  <span className="absolute right-2.5 top-1.5 text-[8px] bg-white/10 px-1 py-0.2 rounded text-slate-400 border border-white/5 font-mono">⌘K</span>
                </div>
                {/* Icons and profile */}
                <div className="flex items-center gap-3">
                  <svg className="w-3.5 h-3.5 text-slate-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
                  <div className="w-5 h-5 rounded-full bg-gradient-to-tr from-purple-500 to-indigo-500 flex items-center justify-center text-[9px] font-bold text-white border border-white/15">
                    JD
                  </div>
                </div>
              </div>

              {/* Dashboard Main Layout */}
              <div className="flex flex-1 min-h-[360px] bg-[#090810]">
                
                {/* Sidebar Mockup */}
                <div className="w-[140px] border-r border-white/5 bg-[#07060d] p-2.5 flex flex-col gap-1.5 text-slate-400">
                  {/* Logo */}
                  <div className="flex items-center gap-1.5 px-1.5 py-1 mb-2">
                    <div className="w-4 h-4 rounded-md bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center text-[10px] text-white font-black">M</div>
                    <span className="font-bold text-[10px] text-white tracking-tight">MailMind AI</span>
                  </div>
                  {/* Menu Items */}
                  <div className="flex items-center justify-between px-2 py-1 bg-white/5 rounded-md text-white font-medium">
                    <span className="flex items-center gap-1.5">
                      <svg className="w-3 h-3 text-indigo-400" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" /></svg>
                      <span>Inbox</span>
                    </span>
                    <span className="text-[9px] bg-indigo-500/20 text-indigo-300 font-bold px-1.5 py-0.2 rounded-full">128</span>
                  </div>
                  
                  <div className="flex items-center gap-1.5 px-2 py-1 hover:text-white transition-colors cursor-default">
                    <svg className="w-3 h-3 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2z" /></svg>
                    <span>Categories</span>
                  </div>
                  
                  <div className="flex items-center justify-between px-2 py-1 hover:text-white transition-colors cursor-default">
                    <span className="flex items-center gap-1.5">
                      <svg className="w-3 h-3 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" /></svg>
                      <span>Newsletters</span>
                    </span>
                    <span className="text-[8px] bg-slate-800 text-slate-300 px-1 py-0.2 rounded">24</span>
                  </div>
                  
                  <div className="flex items-center justify-between px-2 py-1 hover:text-white transition-colors cursor-default">
                    <span className="flex items-center gap-1.5">
                      <svg className="w-3 h-3 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
                      <span>Jobs</span>
                    </span>
                    <span className="text-[8px] bg-slate-800 text-slate-300 px-1 py-0.2 rounded">12</span>
                  </div>

                  <div className="flex items-center justify-between px-2 py-1 hover:text-white transition-colors cursor-default">
                    <span className="flex items-center gap-1.5">
                      <svg className="w-3 h-3 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                      <span>Finance</span>
                    </span>
                    <span className="text-[8px] bg-slate-800 text-slate-300 px-1 py-0.2 rounded">18</span>
                  </div>

                  <div className="flex items-center justify-between px-2 py-1 hover:text-white transition-colors cursor-default">
                    <span className="flex items-center gap-1.5">
                      <svg className="w-3 h-3 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
                      <span>Notifications</span>
                    </span>
                    <span className="text-[8px] bg-slate-800 text-slate-300 px-1 py-0.2 rounded">34</span>
                  </div>

                  <div className="flex items-center gap-1.5 px-2 py-1 hover:text-white transition-colors cursor-default">
                    <svg className="w-3 h-3 text-slate-500" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M8 4H6a2 2 0 00-2 2v12a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-2m-4-1v8m0 0l3-3m-3 3L9 8m-5 5h2.586a1 1 0 01.707.293l2.414 2.414a1 1 0 00.707.293h3.172a1 1 0 00.707-.293l2.414-2.414a1 1 0 01.707-.293H20" /></svg>
                    <span>All Mail</span>
                  </div>

                  <div className="flex items-center justify-between px-2 py-1 bg-gradient-to-r from-violet-600/15 to-transparent border-l-2 border-violet-500/70 text-slate-200 mt-2">
                    <span className="flex items-center gap-1.5">
                      <Sparkles className="w-3 h-3 text-violet-400" />
                      <span className="font-semibold text-violet-300">AI Assistant</span>
                    </span>
                    <span className="text-[7px] bg-violet-500/20 text-violet-400 font-extrabold px-1 rounded">New</span>
                  </div>

                  <div className="mt-auto flex items-center gap-1.5 px-2 py-1 hover:text-white transition-colors cursor-default text-slate-500 text-[10px]">
                    <svg className="w-3 h-3" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                    <span>Settings</span>
                  </div>
                </div>

                {/* Mail Content Pane Mockup */}
                <div className="flex-1 p-3.5 flex flex-col gap-3.5 bg-[#090810]/70 overflow-hidden">
                  
                  {/* Top Info Bar */}
                  <div className="flex items-center justify-between">
                    <div>
                      <h2 className="font-bold text-sm text-white flex items-center gap-1.5">
                        <span>Inbox</span>
                        <span className="text-[10px] text-slate-500 font-normal">128 emails</span>
                      </h2>
                    </div>
                    {/* Inbox categories filter tabs */}
                    <div className="flex gap-1.5 text-[9px] font-medium text-slate-400">
                      <span className="px-2 py-0.5 rounded bg-white/5 text-white">All</span>
                      <span className="px-2 py-0.5 rounded hover:bg-white/5 transition-colors">Primary</span>
                      <span className="px-2 py-0.5 rounded hover:bg-white/5 transition-colors">Unread</span>
                      <span className="px-2 py-0.5 rounded hover:bg-white/5 transition-colors">Flagged</span>
                    </div>
                  </div>

                  {/* Email Rows List */}
                  <div className="flex flex-col gap-1.5">
                    
                    {/* Row 1: Acme */}
                    <div className="flex items-center justify-between p-2.5 rounded-lg bg-white/5 border border-white/5 hover:bg-white/10 transition-colors">
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="w-5 h-5 rounded-full bg-violet-600/30 flex items-center justify-center font-bold text-[9px] text-violet-300">A</div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5">
                            <span className="font-semibold text-slate-200 text-[10px]">Alex from Acme</span>
                            <span className="px-1.5 py-0.2 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[8px] font-medium scale-[0.9] origin-left">Work</span>
                          </div>
                          <div className="text-[9px] text-slate-300 font-medium truncate">Q3 Launch Plan Update</div>
                          <div className="text-[8px] text-slate-500 truncate">Alex shared the latest Q3 launch plan and timelines...</div>
                        </div>
                      </div>
                      <div className="text-[8px] text-slate-500 flex-shrink-0 font-medium ml-2">10:30 AM</div>
                    </div>

                    {/* Row 2: LinkedIn */}
                    <div className="flex items-center justify-between p-2.5 rounded-lg bg-white/5 border border-white/5 hover:bg-white/10 transition-colors">
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="w-5 h-5 rounded-full bg-blue-600/30 flex items-center justify-center font-bold text-[9px] text-blue-300">L</div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5">
                            <span className="font-semibold text-slate-200 text-[10px]">LinkedIn Jobs</span>
                            <span className="px-1.5 py-0.2 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 text-[8px] font-medium scale-[0.9] origin-left">Jobs</span>
                          </div>
                          <div className="text-[9px] text-slate-300 font-medium truncate">5 new job recommendations</div>
                          <div className="text-[8px] text-slate-500 truncate">New roles match your profile: Backend Engineer...</div>
                        </div>
                      </div>
                      <div className="text-[8px] text-slate-500 flex-shrink-0 font-medium ml-2">9:15 AM</div>
                    </div>

                    {/* Row 3: Stripe */}
                    <div className="flex items-center justify-between p-2.5 rounded-lg bg-white/5 border border-white/5 hover:bg-white/10 transition-colors">
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="w-5 h-5 rounded-full bg-violet-600/30 flex items-center justify-center font-bold text-[9px] text-purple-300">S</div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5">
                            <span className="font-semibold text-slate-200 text-[10px]">Stripe</span>
                            <span className="px-1.5 py-0.2 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 text-[8px] font-medium scale-[0.9] origin-left">Finance</span>
                          </div>
                          <div className="text-[9px] text-slate-300 font-medium truncate">Your payment of $49.00 was successful</div>
                          <div className="text-[8px] text-slate-500 truncate">Receipt for the subscription to Stripe Pro Plan...</div>
                        </div>
                      </div>
                      <div className="text-[8px] text-slate-500 flex-shrink-0 font-medium ml-2">8:45 AM</div>
                    </div>

                    {/* Row 4: Product Hunt */}
                    <div className="flex items-center justify-between p-2.5 rounded-lg bg-white/5 border border-white/5 hover:bg-white/10 transition-colors">
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="w-5 h-5 rounded-full bg-red-600/20 flex items-center justify-center font-bold text-[9px] text-red-300">P</div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5">
                            <span className="font-semibold text-slate-200 text-[10px]">Product Hunt Daily</span>
                            <span className="px-1.5 py-0.2 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 text-[8px] font-medium scale-[0.9] origin-left">Newsletter</span>
                          </div>
                          <div className="text-[9px] text-slate-300 font-medium truncate">Top 5 products for you</div>
                          <div className="text-[8px] text-slate-500 truncate">A curated list of trending products for Product Hunt...</div>
                        </div>
                      </div>
                      <div className="text-[8px] text-slate-500 flex-shrink-0 font-medium ml-2">Yesterday</div>
                    </div>

                    {/* Row 5: Google Security */}
                    <div className="flex items-center justify-between p-2.5 rounded-lg bg-white/5 border border-white/5 hover:bg-white/10 transition-colors">
                      <div className="flex items-center gap-2 min-w-0">
                        <div className="w-5 h-5 rounded-full bg-slate-600/30 flex items-center justify-center font-bold text-[9px] text-slate-300">G</div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5">
                            <span className="font-semibold text-slate-200 text-[10px]">Google Security</span>
                            <span className="px-1.5 py-0.2 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20 text-[8px] font-medium scale-[0.9] origin-left">Notifications</span>
                          </div>
                          <div className="text-[9px] text-slate-300 font-medium truncate">Security alert</div>
                          <div className="text-[8px] text-slate-500 truncate">A new sign-in on Windows from Chrome...</div>
                        </div>
                      </div>
                      <div className="text-[8px] text-slate-500 flex-shrink-0 font-medium ml-2">Yesterday</div>
                    </div>

                  </div>

                  {/* Bottom 3 Cards Mockup */}
                  <div className="grid grid-cols-3 gap-2 mt-auto">
                    
                    {/* Card 1: AI Summary */}
                    <div className="p-2.5 rounded-xl bg-[#0b0a15] border border-white/5 flex flex-col justify-between">
                      <div>
                        <span className="text-[8px] font-semibold text-indigo-400 uppercase tracking-wider flex items-center gap-1">
                          <Sparkles className="w-2.5 h-2.5 text-indigo-400" />
                          <span>AI Summary</span>
                        </span>
                        <div className="text-[9px] font-medium text-slate-300 mt-1 leading-normal">
                          You have <span className="text-white font-semibold">12</span> unread emails. <br />
                          <span className="text-indigo-300 font-semibold">3</span> require attention.
                        </div>
                      </div>
                      <div className="mt-1 text-slate-600 h-4">
                        {/* Mini Sparkline Graph */}
                        <svg className="w-full h-full text-indigo-500" viewBox="0 0 100 20" fill="none" stroke="currentColor" strokeWidth="1.5">
                          <path d="M0 15 Q 15 5, 30 12 T 60 4 T 90 16 T 100 8" strokeLinecap="round" />
                        </svg>
                      </div>
                    </div>

                    {/* Card 2: Top Category */}
                    <div className="p-2.5 rounded-xl bg-[#0b0a15] border border-white/5 flex flex-col justify-between">
                      <div>
                        <span className="text-[8px] font-semibold text-cyan-400 uppercase tracking-wider flex items-center gap-1">
                          <Sparkles className="w-2.5 h-2.5 text-cyan-400" />
                          <span>Top Category</span>
                        </span>
                        <div className="flex items-center justify-between gap-1.5 mt-1">
                          <div className="text-[9px] font-medium text-slate-300">
                            <span className="text-xl font-bold text-white leading-none">52%</span>
                            <div className="text-[7px] text-slate-500">of your emails</div>
                          </div>
                        </div>
                      </div>
                      {/* Donut Chart */}
                      <div className="w-6 h-6 relative self-end">
                        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                          <circle cx="18" cy="18" r="15.915" fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="3" />
                          <circle cx="18" cy="18" r="15.915" fill="none" stroke="#6366f1" strokeWidth="3" strokeDasharray="52 48" strokeDashoffset="0" />
                          <circle cx="18" cy="18" r="15.915" fill="none" stroke="#06b6d4" strokeWidth="3" strokeDasharray="20 80" strokeDashoffset="-52" />
                        </svg>
                      </div>
                    </div>

                    {/* Card 3: Time Saved */}
                    <div className="p-2.5 rounded-xl bg-[#0b0a15] border border-white/5 flex flex-col justify-between">
                      <div>
                        <span className="text-[8px] font-semibold text-purple-400 uppercase tracking-wider flex items-center gap-1">
                          <svg className="w-2.5 h-2.5 text-purple-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                          <span>Time Saved</span>
                        </span>
                        <div className="text-[9px] font-medium text-slate-300 mt-1 leading-normal">
                          <span className="text-xl font-bold text-white leading-none">18.6 hrs</span>
                          <div className="text-[7px] text-slate-500">this week</div>
                        </div>
                      </div>
                      <div className="mt-1 text-slate-600 h-4">
                        {/* Mini Sparkline Graph */}
                        <svg className="w-full h-full text-purple-500" viewBox="0 0 100 20" fill="none" stroke="currentColor" strokeWidth="1.5">
                          <path d="M0 10 Q 20 18, 40 8 T 70 14 T 100 4" strokeLinecap="round" />
                        </svg>
                      </div>
                    </div>

                  </div>

                </div>

              </div>

            </div>

          </div>

        </div>

        {/* Client Logos Section */}
        <div className="mt-28 text-center animate-fade-in" style={{ animationDelay: '0.4s' }}>
          <p className="text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-6">
            Trusted by professionals who value focus and clarity
          </p>
          <div className="flex flex-wrap items-center justify-center gap-x-12 gap-y-6 text-slate-500 text-sm font-medium">
            <div className="flex items-center gap-2 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-300 cursor-default">
              <span className="font-extrabold text-base tracking-tight text-white/70">Google</span>
            </div>
            <div className="flex items-center gap-2 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-300 cursor-default">
              <span className="font-extrabold text-base tracking-tight text-white/70">stripe</span>
            </div>
            <div className="flex items-center gap-2 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-300 cursor-default">
              <span className="font-extrabold text-base tracking-tight text-white/70">N Notion</span>
            </div>
            <div className="flex items-center gap-2 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-300 cursor-default">
              <span className="font-extrabold text-base tracking-tight text-white/70">linear</span>
            </div>
            <div className="flex items-center gap-2 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-300 cursor-default">
              <span className="font-extrabold text-base tracking-tight text-white/70">▲ Vercel</span>
            </div>
          </div>
        </div>

        {/* Problem & Solution Section */}
        <div id="features" className="mt-36 pt-10 relative">
          
          {/* THE PROBLEM */}
          <div className="text-center max-w-4xl mx-auto mb-16 px-4">
            <span className="px-4 py-1.5 rounded-full border border-purple-500/30 bg-purple-500/10 text-purple-400 text-[10px] font-extrabold uppercase tracking-widest">
              The Problem
            </span>
            <h2 className="text-3xl md:text-5xl font-extrabold text-slate-100 tracking-tight mt-6 mb-4">
              Email overload is <span className="bg-gradient-to-r from-blue-400 via-indigo-400 to-purple-400 bg-clip-text text-transparent">slowing you down</span>
            </h2>
            <p className="text-slate-400 text-sm md:text-base max-w-xl mx-auto font-medium">
              Important messages get lost. Context is scattered. Time is wasted.
            </p>
          </div>

          {/* Problem Cards */}
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6 max-w-6xl mx-auto px-6">
            
            {/* Card 1: Too Many Emails */}
            <div className="p-6 rounded-2xl bg-white/[0.01] border border-white/5 hover:border-purple-500/20 hover:bg-white/[0.02] transition-all duration-300 flex flex-col gap-4 text-left">
              <div className="w-12 h-12 rounded-full bg-purple-500/10 border border-purple-500/20 flex items-center justify-center relative self-start">
                <Mail className="w-5 h-5 text-purple-400" />
                <span className="absolute -top-1.5 -right-1.5 bg-red-500 text-white text-[9px] font-extrabold px-1.5 py-0.5 rounded-full shadow-lg border border-[#080710]">
                  99+
                </span>
              </div>
              <h3 className="text-base font-bold text-slate-100">Too Many Emails</h3>
              <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                Hundreds of unread emails make it hard to focus on what truly matters.
              </p>
            </div>

            {/* Card 2: Scattered Conversations */}
            <div className="p-6 rounded-2xl bg-white/[0.01] border border-white/5 hover:border-purple-500/20 hover:bg-white/[0.02] transition-all duration-300 flex flex-col gap-4 text-left">
              <div className="w-12 h-12 rounded-full bg-purple-500/10 border border-purple-500/20 flex items-center justify-center self-start">
                <svg className="w-5 h-5 text-purple-400" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h3 className="text-base font-bold text-slate-100">Scattered Conversations</h3>
              <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                Threaded conversations are hard to follow and context gets lost.
              </p>
            </div>

            {/* Card 3: Time Consuming */}
            <div className="p-6 rounded-2xl bg-white/[0.01] border border-white/5 hover:border-amber-500/20 hover:bg-white/[0.02] transition-all duration-300 flex flex-col gap-4 text-left">
              <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/20 flex items-center justify-center self-start">
                <svg className="w-5 h-5 text-amber-400" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-base font-bold text-slate-100">Time Consuming</h3>
              <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                Manually reading, summarizing and replying eats up hours every day.
              </p>
            </div>

            {/* Card 4: Hard to Find Answers */}
            <div className="p-6 rounded-2xl bg-white/[0.01] border border-white/5 hover:border-emerald-500/20 hover:bg-white/[0.02] transition-all duration-300 flex flex-col gap-4 text-left">
              <div className="w-12 h-12 rounded-full bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center self-start">
                <svg className="w-5 h-5 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-base font-bold text-slate-100">Hard to Find Answers</h3>
              <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                Finding that one email or detail from weeks ago feels almost impossible.
              </p>
            </div>

          </div>

          {/* Connector arrow */}
          <div className="flex flex-col items-center my-16 relative">
            <div className="w-[1px] h-14 bg-gradient-to-b from-indigo-500/50 via-purple-500/30 to-transparent" />
            <div 
              className="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 border border-indigo-400/30 flex items-center justify-center shadow-[0_0_20px_rgba(99,102,241,0.5)] z-10 hover:scale-110 transition-transform duration-300 cursor-default"
            >
              <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" strokeWidth="3" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
              </svg>
            </div>
            <div className="w-[1px] h-14 bg-gradient-to-b from-transparent via-purple-500/30 to-emerald-500/50" />
          </div>

          {/* THE SOLUTION */}
          <div className="max-w-6xl mx-auto px-6">
            
            <div className="grid lg:grid-cols-12 gap-12 items-center mb-16">
              
              {/* Solution Title / Heading */}
              <div className="lg:col-span-6 text-left">
                <span className="px-4 py-1.5 rounded-full border border-emerald-500/30 bg-emerald-500/10 text-emerald-400 text-[10px] font-extrabold uppercase tracking-widest">
                  The Solution
                </span>
                <h2 className="text-3xl md:text-5xl font-extrabold text-slate-100 tracking-tight mt-6 mb-4 leading-tight">
                  MailMind AI <span className="bg-gradient-to-r from-blue-400 via-indigo-400 to-purple-400 bg-clip-text text-transparent">solves it for you</span>
                </h2>
                <p className="text-slate-400 text-sm md:text-base font-medium leading-relaxed max-w-lg">
                  AI that understands your inbox, organizes it intelligently, and helps you take action—faster.
                </p>
              </div>

              {/* Solution Right Side Graphic */}
              <div className="lg:col-span-6 relative flex justify-center items-center tilt-perspective select-none">
                
                {/* Ambient glow in background */}
                <div className="absolute w-[85%] h-[85%] bg-gradient-to-tr from-cyan-500/10 via-purple-500/10 to-indigo-500/20 blur-[50px] opacity-70" />

                {/* Main panel (Simplified Inbox Mockup) */}
                <div className="w-full max-w-[420px] rounded-2xl bg-[#090810]/95 border border-white/5 p-4 flex flex-col gap-3.5 shadow-2xl relative dashboard-mockup">
                  
                  {/* Window control dots */}
                  <div className="flex items-center justify-between border-b border-white/5 pb-2.5">
                    <div className="flex gap-1.5">
                      <div className="w-2 h-2 rounded-full bg-red-500/40" />
                      <div className="w-2 h-2 rounded-full bg-yellow-500/40" />
                      <div className="w-2 h-2 rounded-full bg-green-500/40" />
                    </div>
                    <span className="font-bold text-[9px] text-slate-500 tracking-wider uppercase">Inbox</span>
                    <div className="w-2 h-2 rounded-full bg-slate-700" />
                  </div>

                  {/* Sidebar / List split */}
                  <div className="flex gap-4">
                    
                    {/* Minimal Sidebar list */}
                    <div className="w-[110px] flex flex-col gap-1.5 text-[8px] text-slate-500 font-semibold border-r border-white/5 pr-2.5">
                      <div className="flex items-center gap-1.5 px-1.5 py-0.5 rounded bg-white/5 text-white">
                        <Sparkles className="w-3 h-3 text-indigo-400" />
                        <span>Summary</span>
                      </div>
                      <div className="flex items-center gap-1.5 px-1.5 py-0.5 hover:text-white transition-colors">
                        <svg className="w-2.5 h-2.5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2z" />
                        </svg>
                        <span>Categories</span>
                      </div>
                      <div className="flex items-center gap-1.5 px-1.5 py-0.5 hover:text-white transition-colors">
                        <svg className="w-2.5 h-2.5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                        </svg>
                        <span>Top Senders</span>
                      </div>
                      <div className="flex items-center gap-1.5 px-1.5 py-0.5 hover:text-white transition-colors">
                        <Sparkles className="w-3 h-3 text-purple-400 animate-pulse" />
                        <span>AI Assistant</span>
                      </div>
                    </div>

                    {/* Minimal Inbox rows */}
                    <div className="flex-1 flex flex-col gap-2.5 py-0.5">
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-1.5 flex-1 min-w-0">
                          <div className="w-3.5 h-3.5 rounded-full bg-slate-800 flex items-center justify-center text-[7px] text-slate-400 font-bold">A</div>
                          <div className="h-1.5 bg-slate-800 rounded-full w-14" />
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="px-1.5 py-0.2 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[7px] font-semibold">Work</span>
                          <span className="text-[7px] text-slate-600 font-medium">10:30 AM</span>
                        </div>
                      </div>
                      
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-1.5 flex-1 min-w-0">
                          <div className="w-3.5 h-3.5 rounded-full bg-slate-800 flex items-center justify-center text-[7px] text-slate-400 font-bold">S</div>
                          <div className="h-1.5 bg-slate-800 rounded-full w-10" />
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="px-1.5 py-0.2 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 text-[7px] font-semibold">Finance</span>
                          <span className="text-[7px] text-slate-600 font-medium">9:15 AM</span>
                        </div>
                      </div>

                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-1.5 flex-1 min-w-0">
                          <div className="w-3.5 h-3.5 rounded-full bg-slate-800 flex items-center justify-center text-[7px] text-slate-400 font-bold">L</div>
                          <div className="h-1.5 bg-slate-800 rounded-full w-12" />
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="px-1.5 py-0.2 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 text-[7px] font-semibold">Jobs</span>
                          <span className="text-[7px] text-slate-600 font-medium">8:45 AM</span>
                        </div>
                      </div>

                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-center gap-1.5 flex-1 min-w-0">
                          <div className="w-3.5 h-3.5 rounded-full bg-slate-800 flex items-center justify-center text-[7px] text-slate-400 font-bold">G</div>
                          <div className="h-1.5 bg-slate-800 rounded-full w-8" />
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="px-1.5 py-0.2 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20 text-[7px] font-semibold">Personal</span>
                          <span className="text-[7px] text-slate-600 font-medium">Yesterday</span>
                        </div>
                      </div>
                    </div>

                  </div>

                </div>

                {/* Floating Element 1: 3D Sparkle Box */}
                <div className="absolute top-[5%] right-[-5%] z-20 animate-float-star pointer-events-none">
                  <div className="relative p-2.5 rounded-2xl bg-gradient-to-br from-indigo-600/90 to-purple-600/90 border border-white/10 shadow-2xl flex items-center justify-center">
                    <Sparkles className="w-5 h-5 text-white animate-pulse" />
                  </div>
                </div>

                {/* Floating Element 2: AI Summary Card */}
                <div className="absolute bottom-[28%] left-[-10%] z-20 animate-float-envelope max-w-[170px] pointer-events-none">
                  <div className="p-3 rounded-xl bg-gradient-to-br from-[#0e0c1f]/95 to-[#0b0a15]/95 border border-indigo-500/35 shadow-2xl backdrop-blur-md flex flex-col gap-1.5">
                    <span className="text-[7px] font-bold text-indigo-400 uppercase tracking-wider flex items-center gap-1">
                      <Sparkles className="w-2 h-2 text-indigo-400" />
                      <span>AI Summary</span>
                    </span>
                    <div className="text-[8px] font-semibold text-slate-200 leading-normal">
                      3 key updates from your unread emails.
                    </div>
                    <svg className="w-full h-3 text-indigo-500" viewBox="0 0 100 20" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M0 15 Q 25 5, 50 12 T 100 8" strokeLinecap="round" />
                    </svg>
                  </div>
                </div>

                {/* Floating Element 3: 3D Donut Chart */}
                <div className="absolute bottom-[5%] left-[20%] z-30 animate-float-star pointer-events-none" style={{ animationDelay: '2.5s' }}>
                  <div className="w-14 h-14 rounded-full bg-[#0b0a15]/90 border border-cyan-500/20 p-2.5 flex items-center justify-center shadow-2xl backdrop-blur-md">
                    <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                      <circle cx="18" cy="18" r="15.915" fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="4" />
                      <circle cx="18" cy="18" r="15.915" fill="none" stroke="#6366f1" strokeWidth="4" strokeDasharray="52 48" strokeDashoffset="0" />
                      <circle cx="18" cy="18" r="15.915" fill="none" stroke="#06b6d4" strokeWidth="4" strokeDasharray="28 72" strokeDashoffset="-52" />
                    </svg>
                  </div>
                </div>

                {/* Floating Element 4: Ask MailMind AI chat card */}
                <div className="absolute bottom-[10%] right-[-10%] z-20 animate-float-envelope max-w-[190px] pointer-events-none" style={{ animationDelay: '1s' }}>
                  <div className="p-3.5 rounded-xl bg-gradient-to-br from-[#0e0c1f]/95 to-[#0b0a15]/95 border border-purple-500/35 shadow-2xl backdrop-blur-md flex flex-col gap-1.5">
                    <span className="text-[7px] font-bold text-purple-400 uppercase tracking-wider flex items-center gap-1">
                      <svg className="w-2.5 h-2.5 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span>Ask MailMind AI</span>
                    </span>
                    <div className="text-[9px] font-semibold text-slate-200 leading-normal mb-1">
                      What did Alex reply about the Q3 plan?
                    </div>
                    <div className="w-full flex items-center justify-end">
                      <div className="w-5 h-5 rounded-md bg-indigo-600 hover:bg-indigo-500 transition-colors flex items-center justify-center text-white text-[9px] font-bold">
                        &gt;
                      </div>
                    </div>
                  </div>
                </div>

              </div>

            </div>

            {/* Solution Feature Columns Grid (4 Columns) */}
            <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6 mt-16 text-left">
              
              {/* Feature 1: Understands */}
              <div className="p-6 rounded-2xl bg-[#090810]/40 border border-white/5 flex flex-col gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center">
                  <Brain className="w-5 h-5 text-purple-400" />
                </div>
                <h4 className="text-sm font-bold text-slate-100">Understands</h4>
                <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                  AI reads and understands your emails and threads like a human.
                </p>
              </div>

              {/* Feature 2: Organizes */}
              <div className="p-6 rounded-2xl bg-[#090810]/40 border border-white/5 flex flex-col gap-3">
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
                  <Folder className="w-5 h-5 text-emerald-400" />
                </div>
                <h4 className="text-sm font-bold text-slate-100">Organizes</h4>
                <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                  Automatically summarizes, categorizes, and prioritizes what matters.
                </p>
              </div>

              {/* Feature 3: Responds */}
              <div className="p-6 rounded-2xl bg-[#090810]/40 border border-white/5 flex flex-col gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center">
                  <Send className="w-5 h-5 text-purple-400" />
                </div>
                <h4 className="text-sm font-bold text-slate-100">Responds</h4>
                <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                  Generate smart replies and take action with full context.
                </p>
              </div>

              {/* Feature 4: Finds Answers */}
              <div className="p-6 rounded-2xl bg-[#090810]/40 border border-white/5 flex flex-col gap-3">
                <div className="w-10 h-10 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center">
                  <Search className="w-5 h-5 text-cyan-400" />
                </div>
                <h4 className="text-sm font-bold text-slate-100">Finds Answers</h4>
                <p className="text-xs md:text-sm text-slate-400 leading-relaxed">
                  Ask anything. Get accurate answers from your own email data.
                </p>
              </div>

            </div>

            {/* Bottom Dark Trust Bar */}
            <div className="mt-16 max-w-4xl mx-auto rounded-2xl bg-[#090810]/80 border border-white/5 p-4 flex flex-col sm:flex-row items-center justify-around text-xs font-semibold text-slate-400 shadow-xl gap-4">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-indigo-400" />
                <span>100% private & secure</span>
              </div>
              <div className="hidden sm:block text-slate-800">|</div>
              <div className="flex items-center gap-2">
                <Lock className="w-4 h-4 text-purple-400" />
                <span>Only your data is used</span>
              </div>
              <div className="hidden sm:block text-slate-800">|</div>
              <div className="flex items-center gap-2">
                <svg className="w-4 h-4 text-cyan-400" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                <span>You're always in control</span>
              </div>
            </div>

          </div>

        </div>

      </main>

      {/* Built with Tech Stack Banner */}
      <section className="py-20 border-t border-white/5 bg-[#07060c]">
        <div className="max-w-4xl mx-auto text-center px-6">
          <h2 className="text-xs font-bold uppercase tracking-widest text-indigo-400 mb-8">Architected for Speed & Reliability</h2>
          <div className="flex flex-wrap items-center justify-center gap-4 text-slate-400">
            {['Next.js 15', 'Spring Boot 3', 'Supabase DB', 'pgvector', 'Gemini API', 'NVIDIA NIM', 'Tailwind CSS'].map(t => (
              <span key={t} className="px-3.5 py-1.5 rounded-lg bg-white/[0.03] border border-white/5 text-xs font-medium text-slate-300">
                {t}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-8 border-t border-white/5 bg-[#050409]">
        <div className="max-w-7xl mx-auto text-center text-[11px] text-slate-500 px-6">
          <p>MailMind AI — Technical Assessment for Repeatless AI Automation Executive</p>
        </div>
      </footer>
    </div>
  );
}
