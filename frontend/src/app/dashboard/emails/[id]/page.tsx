'use client';
import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import { CATEGORY_COLORS, CATEGORY_ICONS } from '@/lib/types';
import type { EmailDetail } from '@/lib/types';

export default function EmailDetailPage() {
  const params = useParams();
  const id = params.id as string;

  const [email, setEmail] = useState<EmailDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [replyMode, setReplyMode] = useState(false);
  const [replyInstruction, setReplyInstruction] = useState('');
  const [generatedReply, setGeneratedReply] = useState('');
  const [generating, setGenerating] = useState(false);
  const [sending, setSending] = useState(false);
  const [draftId, setDraftId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'html' | 'text'>('html');

  const loadEmail = () => {
    setLoading(true);
    api.getEmail(id)
      .then(setEmail)
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (id) loadEmail();
  }, [id]);

  useEffect(() => {
    if (email) {
      setActiveTab(email.bodyHtml ? 'html' : 'text');
    }
  }, [email]);

  const handleGenerateReply = async () => {
    if (!email) return;
    setGenerating(true);
    try {
      const res = await api.generateReply(email.id, email.gmailThreadId, replyInstruction);
      setGeneratedReply(res.bodyText);
      setDraftId(res.draftId);
    } catch (err) {
      console.error(err);
    } finally {
      setGenerating(false);
    }
  };

  const handleSendReply = async () => {
    if (!draftId || !email) return;
    setSending(true);
    try {
      await api.sendReply(draftId, "Re: " + email.subject, generatedReply, [email.senderEmail]);
      alert("Reply sent successfully!");
      setReplyMode(false);
      setGeneratedReply("");
      setReplyInstruction("");
      loadEmail();
    } catch (err) {
      console.error(err);
      alert("Failed to send reply");
    } finally {
      setSending(false);
    }
  };

  const handleSummarizeThread = async () => {
    if (!email) return;
    try {
      const res = await api.summarizeThread(email.gmailThreadId);
      setEmail(prev => prev ? { ...prev, aiSummary: res.summary } : null);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCategorize = async () => {
    if (!email) return;
    try {
      const res = await api.categorize(email.id);
      setEmail(prev => prev ? { ...prev, aiCategory: res.category } : null);
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return (
    <div className="animate-fade-in max-w-4xl">
      <div className="h-4 w-28 bg-surface-lighter/50 rounded animate-pulse mb-6" />

      <div className="card !p-0 overflow-hidden">
        <div className="p-6 border-b border-border space-y-4">
          <div className="flex items-start justify-between">
            <div className="h-7 w-2/3 bg-surface-lighter/50 rounded animate-pulse" />
            <div className="h-6 w-24 bg-surface-lighter/50 rounded-full animate-pulse" />
          </div>
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-surface-lighter/50 animate-pulse shrink-0" />
            <div className="flex-1 space-y-2">
              <div className="h-4 w-1/3 bg-surface-lighter/50 rounded animate-pulse" />
              <div className="h-3 w-1/4 bg-surface-lighter/50 rounded animate-pulse" />
            </div>
          </div>
        </div>

        <div className="mx-6 mt-6 p-4 rounded-xl bg-surface-light border border-border/50 space-y-2">
          <div className="h-4 w-24 bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-3 w-full bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-3 w-5/6 bg-surface-lighter/50 rounded animate-pulse" />
        </div>

        <div className="p-6 space-y-3">
          <div className="h-4 w-full bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-4 w-full bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-4 w-11/12 bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-4 w-4/5 bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-4 w-full bg-surface-lighter/50 rounded animate-pulse" />
          <div className="h-4 w-3/4 bg-surface-lighter/50 rounded animate-pulse" />
        </div>

        <div className="p-6 border-t border-border flex gap-3">
          <div className="h-10 w-24 bg-surface-lighter/50 rounded-xl animate-pulse" />
          <div className="h-10 w-36 bg-surface-lighter/50 rounded-xl animate-pulse" />
          <div className="h-10 w-24 bg-surface-lighter/50 rounded-xl animate-pulse" />
        </div>
      </div>
    </div>
  );

  if (!email) return (
    <div className="text-center py-20 text-text-muted">
      <p className="text-4xl mb-3">⚠️</p>
      <p>Email not found</p>
      <Link href="/dashboard/emails" className="text-sm text-primary mt-4 inline-block">Back to Inbox</Link>
    </div>
  );

  return (
    <div className="animate-fade-in max-w-4xl">
      <Link href="/dashboard/emails" className="text-sm text-primary-light hover:text-primary mb-4 inline-block">← Back to Inbox</Link>

      <div className="card !p-0 overflow-hidden">
        {/* Header */}
        <div className="p-6 border-b border-border">
          <div className="flex items-start justify-between mb-4">
            <h1 className="text-xl font-bold">{email.subject || '(No Subject)'}</h1>
            {email.aiCategory && (
              <span className={`category-badge border ${CATEGORY_COLORS[email.aiCategory] || CATEGORY_COLORS['Uncategorized']}`}>
                {CATEGORY_ICONS[email.aiCategory] || '📧'} {email.aiCategory}
              </span>
            )}
          </div>
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary/30 to-accent/30 flex items-center justify-center text-lg font-bold">
              {email.senderName?.charAt(0) || email.senderEmail?.charAt(0) || '?'}
            </div>
            <div>
              <div className="font-medium">
                {email.senderName || 'Unknown Sender'}{' '}
                <span className="text-text-dim font-normal">&lt;{email.senderEmail}&gt;</span>
              </div>
              <div className="text-xs text-text-dim">
                To: {email.recipientEmails?.join(', ') || 'Me'} •{' '}
                {email.receivedAt ? new Date(email.receivedAt).toLocaleString() : ''}
              </div>
            </div>
          </div>
        </div>

        {/* AI Summary */}
        {email.aiSummary && (
          <div className="mx-6 mt-4 p-4 rounded-xl bg-primary/5 border border-primary/20">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-sm">🧠</span>
              <span className="text-sm font-semibold text-primary-light">AI Summary</span>
            </div>
            <p className="text-sm text-text-muted leading-relaxed">{email.aiSummary}</p>
          </div>
        )}

        {/* Tab Selector if HTML is available */}
        {email.bodyHtml && (
          <div className="flex gap-2 px-6 mb-2 border-b border-border/50 pb-2">
            <button
              onClick={() => setActiveTab('html')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'html'
                  ? 'bg-primary text-white shadow-sm'
                  : 'text-text-muted hover:text-text hover:bg-surface/50'
              }`}
            >
              🌐 HTML View
            </button>
            <button
              onClick={() => setActiveTab('text')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'text'
                  ? 'bg-primary text-white shadow-sm'
                  : 'text-text-muted hover:text-text hover:bg-surface/50'
              }`}
            >
              📄 Plain Text
            </button>
          </div>
        )}

        {/* Body */}
        <div className="p-6">
          {activeTab === 'html' && email.bodyHtml ? (
            <div className="w-full rounded-xl overflow-hidden border border-border bg-white">
              <iframe
                srcDoc={email.bodyHtml}
                title="Email Content"
                className="w-full min-h-[500px]"
                sandbox="allow-popups allow-popups-to-escape-sandbox"
              />
            </div>
          ) : (
            <pre className="whitespace-pre-wrap text-sm text-text-muted leading-relaxed font-[inherit]">
              {email.bodyText || 'No body content.'}
            </pre>
          )}
        </div>

        {/* Actions */}
        <div className="p-6 border-t border-border flex gap-3">
          <button onClick={() => setReplyMode(!replyMode)} className="btn-primary text-sm">
            ✍️ AI Reply
          </button>
          <button onClick={handleSummarizeThread} className="btn-secondary text-sm">
            📋 Summarize Thread
          </button>
          <button onClick={handleCategorize} className="btn-secondary text-sm">
            🏷️ Categorize
          </button>
        </div>

        {/* Reply section */}
        {replyMode && (
          <div className="p-6 border-t border-border bg-surface/50">
            <h3 className="font-semibold mb-3">Generate AI Reply</h3>
            <textarea
              className="input-field mb-3"
              rows={3}
              placeholder="Enter your reply instruction (e.g., 'Acknowledge the roadmap, confirm timeline, and ask about budget')"
              value={replyInstruction}
              onChange={(e) => setReplyInstruction(e.target.value)}
            />
            <button
              onClick={handleGenerateReply}
              disabled={generating || !replyInstruction}
              className="btn-primary text-sm disabled:opacity-50 mb-4"
            >
              {generating ? '⏳ Generating...' : '🚀 Generate Reply'}
            </button>
            {generatedReply && (
              <div className="mt-4">
                <h4 className="text-sm font-semibold text-text-muted mb-2">Generated Reply:</h4>
                <textarea
                  className="input-field"
                  rows={10}
                  value={generatedReply}
                  onChange={(e) => setGeneratedReply(e.target.value)}
                />
                <div className="flex gap-2 mt-3">
                  <button onClick={handleSendReply} disabled={sending} className="btn-primary text-sm">
                    {sending ? 'Sending...' : '📤 Send Reply'}
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
