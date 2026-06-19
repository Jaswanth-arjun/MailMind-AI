'use client';
import { useState } from 'react';
import { api } from '@/lib/api';

export default function ComposePage() {
  const [prompt, setPrompt] = useState('');
  const [to, setTo] = useState('');
  const [subject, setSubject] = useState('');
  const [body, setBody] = useState('');
  const [generating, setGenerating] = useState(false);
  const [sending, setSending] = useState(false);
  const [draftId, setDraftId] = useState<string | null>(null);

  const handleGenerate = async () => {
    if (!prompt) return;
    setGenerating(true);
    try {
      const res = await api.generateDraft(prompt, to ? [to] : undefined, subject || undefined);
      setSubject(res.subject || subject || 'Draft Email');
      setBody(res.bodyText);
      setDraftId(res.draftId);
    } catch (err) {
      console.error(err);
      alert('Failed to generate draft. Please try again.');
    } finally {
      setGenerating(false);
    }
  };

  const handleSend = async () => {
    if (!to || !body) return;
    setSending(true);
    try {
      if (draftId) {
        await api.sendDraft(draftId, subject, body, [to]);
      } else {
        // Generate a temporary draft id if none exists
        const draftRes = await api.generateDraft(prompt || "Manual compose", [to], subject);
        await api.sendDraft(draftRes.draftId, subject, body, [to]);
      }
      alert('Email sent successfully!');
      setTo('');
      setSubject('');
      setBody('');
      setPrompt('');
      setDraftId(null);
    } catch (err) {
      console.error(err);
      alert('Failed to send email.');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="animate-fade-in max-w-3xl">
      <h1 className="text-2xl font-bold mb-6">✍️ Compose Email</h1>

      <div className="card !p-0 overflow-hidden">
        {/* Compose form */}
        <div className="p-6 space-y-4">
          <div>
            <label className="text-sm font-medium text-text-muted mb-1 block">To</label>
            <input
              type="email"
              className="input-field"
              placeholder="recipient@example.com"
              value={to}
              onChange={(e) => setTo(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm font-medium text-text-muted mb-1 block">Subject</label>
            <input
              type="text"
              className="input-field"
              placeholder="Email subject"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
            />
          </div>

          {/* AI Prompt */}
          <div className="p-4 rounded-xl bg-primary/5 border border-primary/20">
            <div className="flex items-center gap-2 mb-2">
              <span>🧠</span>
              <span className="text-sm font-semibold text-primary-light">AI Draft Generation</span>
            </div>
            <textarea
              className="input-field !bg-surface/50"
              rows={3}
              placeholder="Describe what you want to say (e.g., 'Follow up on our Q3 planning meeting, ask about timeline for the mobile app launch')"
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
            />
            <button
              onClick={handleGenerate}
              disabled={generating || !prompt}
              className="btn-primary text-sm mt-3 disabled:opacity-50"
            >
              {generating ? '⏳ Generating...' : '✨ Generate Draft'}
            </button>
          </div>

          {/* Email body */}
          <div>
            <label className="text-sm font-medium text-text-muted mb-1 block">
              Email Body {draftId && <span className="text-primary-light">(AI Generated — editable)</span>}
            </label>
            <textarea
              className="input-field"
              rows={14}
              placeholder="Email body will appear here..."
              value={body}
              onChange={(e) => setBody(e.target.value)}
            />
          </div>
        </div>

        {/* Actions */}
        <div className="p-6 border-t border-border flex gap-3">
          <button
            onClick={handleSend}
            disabled={sending || !body || !to}
            className="btn-primary text-sm disabled:opacity-50"
          >
            {sending ? '⏳ Sending...' : '📤 Send Email'}
          </button>
          <button
            onClick={() => {
              setBody('');
              setPrompt('');
              setTo('');
              setSubject('');
              setDraftId(null);
            }}
            className="btn-secondary text-sm"
          >
            🗑️ Clear
          </button>
        </div>
      </div>
    </div>
  );
}
