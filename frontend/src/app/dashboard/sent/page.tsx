import MailboxPage from '@/components/MailboxPage';

export default function SentPage() {
  return <MailboxPage title="Sent" icon="Sent" mailbox="sent" emptyText="No sent messages have been synced yet." />;
}
