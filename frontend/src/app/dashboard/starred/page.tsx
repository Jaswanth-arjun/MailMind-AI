import MailboxPage from '@/components/MailboxPage';

export default function StarredPage() {
  return <MailboxPage title="Starred" icon="Starred" mailbox="starred" emptyText="No starred messages found." />;
}
