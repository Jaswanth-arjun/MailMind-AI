import MailboxPage from '@/components/MailboxPage';

export default function DraftsPage() {
  return <MailboxPage title="Drafts" icon="Drafts" mailbox="drafts" emptyText="No Gmail drafts have been synced yet." />;
}
