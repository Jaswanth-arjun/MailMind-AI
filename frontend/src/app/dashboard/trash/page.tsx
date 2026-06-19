import MailboxPage from '@/components/MailboxPage';

export default function TrashPage() {
  return <MailboxPage title="Trash" icon="Trash" mailbox="trash" emptyText="No trash messages have been synced yet." />;
}
