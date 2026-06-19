'use client';
import { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { api } from '@/lib/api';
import type { ChatMessage } from '@/lib/types';
import { 
  Sparkles, 
  Brain, 
  X, 
  SendHorizontal, 
  Loader2, 
  ArrowRight,
  MessageSquare
} from 'lucide-react';

export default function FloatingChat() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string | undefined>(undefined);
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, loading]);

  const handleSubmit = async (text: string) => {
    if (!text.trim() || loading) return;

    const userMsg: ChatMessage = {
      id: Math.random().toString(),
      role: 'user',
      content: text,
      timestamp: new Date().toISOString()
    };

    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const response = await api.chat(text, sessionId);
      if (response.sessionId) {
        setSessionId(response.sessionId);
      }

      const assistantMsg: ChatMessage = {
        id: Math.random().toString(),
        role: 'assistant',
        content: response.answer,
        timestamp: new Date().toISOString(),
        sources: response.sources
      };
      setMessages(prev => [...prev, assistantMsg]);
    } catch (error) {
      const errorMsg: ChatMessage = {
        id: Math.random().toString(),
        role: 'assistant',
        content: 'Sorry, I encountered an error. Please verify that the backend API is running.',
        timestamp: new Date().toISOString()
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const starterPrompts = [
    "Summarize my unread emails",
    "Which companies rejected my application?",
    "Show finance related emails"
  ];

  return (
    <div className="font-sans">
      {/* Floating Action Button (FAB) */}
      <div className="fixed bottom-6 right-6 z-50">
        <button
          onClick={() => setIsOpen(!isOpen)}
          className={`relative w-14 h-14 rounded-[1.25rem] bg-gradient-to-br from-[#6366f1] via-[#8b5cf6] to-[#06b6d4] text-white flex items-center justify-center shadow-[0_6px_24px_rgba(99,102,241,0.4)] hover:shadow-[0_8px_32px_rgba(99,102,241,0.6)] transition-all duration-300 transform hover:scale-105 cursor-pointer group`}
        >
          {/* Pulsing ring outline */}
          <span className="absolute -inset-1 rounded-[1.25rem] bg-[#8b5cf6] opacity-25 filter blur-sm group-hover:opacity-40 animate-pulse-slow pointer-events-none" />
          
          {isOpen ? (
            <X className="w-6 h-6 stroke-[2.5]" />
          ) : (
            <Brain className="w-6 h-6 text-white" />
          )}
        </button>
      </div>

      {/* Backdrop overlay */}
      {isOpen && (
        <div 
          onClick={() => setIsOpen(false)}
          className="fixed inset-0 bg-[#06080f]/80 backdrop-blur-md z-40 animate-fade-in cursor-pointer"
        />
      )}

      {/* Chat Window Overlay */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-start justify-center pt-[10vh] pointer-events-none">
          <div className="pointer-events-auto w-[650px] max-w-[90vw] h-[680px] max-h-[75vh] bg-[#0f111a]/95 border border-[#2b354d]/60 rounded-3xl shadow-[0_24px_60px_rgba(0,0,0,0.8)] flex flex-col overflow-hidden animate-slide-up relative">
            {/* Header */}
            <div className="bg-[#111520]/95 border-b border-[#1a1f2e] px-5 py-4 flex items-center justify-between shrink-0">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-[#6366f1] to-[#8b5cf6] flex items-center justify-center shadow-lg select-none">
                  <Brain className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h4 className="text-[14.5px] font-extrabold text-white leading-tight">MailMind Assistant</h4>
                  <p className="text-[10px] text-[#10b981] font-semibold flex items-center gap-1.5 mt-0.5 leading-none">
                    <span className="w-1.5 h-1.5 rounded-full bg-[#10b981] animate-pulse" />
                    AI Agent Online
                  </p>
                </div>
              </div>
              
              <div className="flex items-center gap-2">
                {messages.length > 0 && (
                  <button
                    onClick={() => setMessages([])}
                    className="text-[#64748b] hover:text-white text-[11px] font-semibold px-2.5 py-1.5 rounded-lg hover:bg-[#181d2a] transition-all cursor-pointer"
                  >
                    Clear Chat
                  </button>
                )}
                <button
                  onClick={() => setIsOpen(false)}
                  className="text-[#64748b] hover:text-white p-1.5 hover:bg-[#181d2a] rounded-lg transition-all cursor-pointer"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Conversation Area */}
            <div className="flex-1 overflow-y-auto p-5 space-y-4 scrollbar-thin">
              {messages.length === 0 ? (
                // Welcome Screen
                <div className="h-full flex flex-col justify-center items-center text-center px-6 py-8">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#6366f1]/15 to-[#8b5cf6]/15 border border-[#2b354d]/50 flex items-center justify-center mb-4 shadow-inner">
                    <Sparkles className="w-7 h-7 text-[#818cf8]" />
                  </div>
                  <h5 className="text-[16px] font-black text-white mb-1">How can I assist you today?</h5>
                  <p className="text-[12px] text-[#8c9bb4] mb-8 max-w-[280px]">Ask questions or summarize info across your email threads.</p>
                  
                  <div className="w-full max-w-[440px] space-y-2.5 text-left">
                    <div className="text-[10px] font-extrabold tracking-wider text-[#4e5e78] uppercase px-1 mb-1">
                      Suggested queries
                    </div>
                    {starterPrompts.map((prompt, idx) => (
                      <button
                        key={idx}
                        onClick={() => handleSubmit(prompt)}
                        className="w-full text-left p-3.5 rounded-xl border border-[#1a1f2e] hover:border-[#6366f1]/35 bg-[#0c0e16] hover:bg-[#111520] text-[#8c9bb4] hover:text-white text-[12px] font-bold transition-all duration-200 cursor-pointer flex items-center justify-between group"
                      >
                        <span>✨ {prompt}</span>
                        <ArrowRight className="w-4 h-4 text-[#4e5e78] group-hover:text-white group-hover:translate-x-0.5 transition-all" />
                      </button>
                    ))}
                  </div>
                </div>
              ) : (
                // Message List
                <div className="space-y-4">
                  {messages.map((msg) => (
                    <div
                      key={msg.id}
                      className={`flex flex-col max-w-[85%] ${msg.role === 'user' ? 'ml-auto items-end' : 'mr-auto items-start'}`}
                    >
                      <div
                        className={`p-3.5 rounded-2xl text-[12.5px] leading-relaxed shadow-sm
                          ${msg.role === 'user'
                            ? 'bg-[#6366f1] text-white rounded-br-none'
                            : 'bg-[#181d2a] text-[#f1f5f9] border border-[#2b354d]/60 rounded-bl-none'
                          }`}
                      >
                        <p className="whitespace-pre-wrap">{msg.content}</p>

                        {/* Cited Sources mapping */}
                        {msg.role === 'assistant' && msg.sources && msg.sources.length > 0 && (
                          <div className="mt-3.5 pt-3 border-t border-[#2b354d]/50 text-[10.5px]">
                            <span className="font-bold text-[#64748b] uppercase tracking-wider block mb-1.5">Cited sources:</span>
                            <div className="space-y-1.5">
                              {msg.sources.map((src, sidx) => (
                                <Link
                                  key={sidx}
                                  href={`/dashboard/emails/${src.emailId}`}
                                  onClick={() => setIsOpen(false)}
                                  className="block text-[#818cf8] hover:underline truncate"
                                >
                                  📄 {src.senderName}: &quot;{src.subject}&quot;
                                </Link>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}

                  {/* Loading state indicator */}
                  {loading && (
                    <div className="flex flex-col max-w-[85%] mr-auto items-start">
                      <div className="p-3.5 bg-[#181d2a] border border-[#2b354d]/60 rounded-2xl rounded-bl-none text-[#8c9bb4] flex items-center gap-2.5 text-[12.5px]">
                        <Loader2 className="w-4 h-4 animate-spin text-[#818cf8]" />
                        <span>Thinking...</span>
                      </div>
                    </div>
                  )}
                  <div ref={chatEndRef} />
                </div>
              )}
            </div>

            {/* Stdin box */}
            <div className="bg-[#111520] border-t border-[#1a1f2e] p-4 shrink-0">
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleSubmit(input);
                }}
                className="relative flex items-center"
              >
                <input
                  type="text"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  placeholder="Message MailMind AI..."
                  className="w-full bg-[#0c0e16] text-[12.5px] text-white placeholder-[#4e5e78] pl-4 pr-12 py-3 rounded-xl border border-[#1a1f2e] focus:border-[#6366f1] focus:outline-none transition-colors"
                />
                <button
                  type="submit"
                  disabled={!input.trim() || loading}
                  className="absolute right-2.5 p-2 rounded-lg bg-gradient-to-r from-[#6366f1] to-[#8b5cf6] text-white hover:brightness-110 disabled:opacity-50 disabled:brightness-100 disabled:cursor-not-allowed transition-all cursor-pointer flex items-center justify-center"
                >
                  <SendHorizontal className="w-4 h-4" />
                </button>
              </form>
              <p className="text-[9px] text-[#64748b] text-center mt-2.5 font-medium leading-none">
                AI-generated responses can contain errors.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
