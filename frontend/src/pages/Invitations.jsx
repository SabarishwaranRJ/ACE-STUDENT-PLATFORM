import React, { useState, useEffect } from 'react';
import { invitationsApi } from '../api/invitations';
import InvitationCard from '../components/invitations/InvitationCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import { Mail, Inbox, Send, Info, UserCheck, ArrowRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/auth';

const Invitations = () => {
  const { user, login } = useAuth();
  const [tab, setTab] = useState('inbox'); // 'inbox' | 'sent'
  const [inbox, setInbox] = useState([]);
  const [sent, setSent] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [switchLoading, setSwitchLoading] = useState(false);

  const loadInvitations = async () => {
    setLoading(true);
    setError('');
    try {
      const [inboxRes, sentRes] = await Promise.all([
        invitationsApi.getMyInvitations(),
        invitationsApi.getSentInvitations(),
      ]);

      if (inboxRes.success) setInbox(inboxRes.data || []);
      if (sentRes.success) setSent(sentRes.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load invitations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvitations();
  }, [user]);

  const handleRespond = async (id, accept) => {
    try {
      await invitationsApi.respondInvitation(id, accept);
      loadInvitations();
    } catch (err) {
      alert(err.message || 'Failed to update invitation');
    }
  };

  const handleWithdraw = async (id) => {
    if (!window.confirm('Withdraw this invitation?')) return;
    try {
      await invitationsApi.withdrawInvitation(id);
      loadInvitations();
    } catch (err) {
      alert(err.message || 'Failed to withdraw invitation');
    }
  };

  // Quick Switch for testing: Switch to Aarav to accept the invitation
  const handleQuickSwitch = async (email) => {
    setSwitchLoading(true);
    try {
      const res = await authApi.login({ email, password: 'password123' });
      if (res.success && res.data) {
        login(res.data.token, {
          studentId: res.data.studentId,
          name: res.data.name,
          email: res.data.email,
        });
      }
    } catch (err) {
      alert('Could not switch account: ' + err.message);
    } finally {
      setSwitchLoading(false);
    }
  };

  const currentList = tab === 'inbox' ? inbox : sent;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs">
        <div className="max-w-2xl">
          <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-accent text-primary border border-border mb-3">
            <Mail className="w-3.5 h-3.5" />
            <span>Communication Center</span>
          </div>
          <h1 className="text-3xl font-black text-foreground tracking-tight">Invitations</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage incoming requests to join teams and track outgoing invitations sent to candidate teammates.
          </p>
        </div>

        {/* Tab Explanation Banner */}
        <div className="mt-6 p-4 bg-accent/40 rounded-[calc(var(--radius)-0.4rem)] border border-border text-xs text-foreground flex items-start space-x-3">
          <Info className="w-4 h-4 text-primary flex-shrink-0 mt-0.5" />
          <div className="leading-relaxed">
            <p className="font-semibold text-foreground">
              {tab === 'inbox'
                ? 'Inbox displays team invitations sent TO YOU by other leaders.'
                : 'Sent displays recruit invitations YOU SENT to other students (they must accept in their inbox).'}
            </p>
            {sent.length > 0 && tab === 'inbox' && (
              <p className="mt-1 text-muted-foreground">
                You have <strong className="text-foreground">{sent.length} outgoing invitation</strong> in your{' '}
                <button
                  onClick={() => setTab('sent')}
                  className="text-primary font-bold underline hover:opacity-80"
                >
                  Sent Tab
                </button>
                .
              </p>
            )}
            {sent.length > 0 && tab === 'sent' && (
              <div className="mt-2 pt-2 border-t border-border flex flex-wrap items-center gap-2">
                <span className="text-muted-foreground">Want to test accepting from Aarav Sharma?</span>
                <button
                  onClick={() => handleQuickSwitch('aarav.sharma@example.edu')}
                  disabled={switchLoading}
                  className="inline-flex items-center space-x-1 px-2.5 py-1 bg-primary hover:bg-[#1a8cd8] text-primary-foreground font-bold rounded-lg text-[11px] shadow-2xs transition"
                >
                  <UserCheck className="w-3 h-3" />
                  <span>Switch to Aarav's Inbox</span>
                  <ArrowRight className="w-3 h-3" />
                </button>
              </div>
            )}
            {user?.email === 'aarav.sharma@example.edu' && (
              <div className="mt-2 pt-2 border-t border-border flex items-center gap-2">
                <span className="text-muted-foreground">Logged in as Aarav. Return to Sabarishwaran?</span>
                <button
                  onClick={() => handleQuickSwitch('janu.sabarish.2011@gmail.com')}
                  disabled={switchLoading}
                  className="px-2.5 py-1 bg-accent text-primary border border-border font-bold rounded-lg text-[11px] hover:bg-muted transition"
                >
                  Switch back to Sabarishwaran
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Tab Selector */}
        <div className="mt-6 flex items-center space-x-2 border-b border-border pb-px">
          <button
            onClick={() => setTab('inbox')}
            className={`flex items-center space-x-2 px-4 py-2.5 rounded-[calc(var(--radius)-0.4rem)] text-sm font-bold transition ${
              tab === 'inbox'
                ? 'bg-primary text-primary-foreground shadow-xs'
                : 'text-muted-foreground hover:text-foreground hover:bg-muted'
            }`}
          >
            <Inbox className="w-4 h-4" />
            <span>Inbox ({inbox.length})</span>
          </button>

          <button
            onClick={() => setTab('sent')}
            className={`flex items-center space-x-2 px-4 py-2.5 rounded-[calc(var(--radius)-0.4rem)] text-sm font-bold transition ${
              tab === 'sent'
                ? 'bg-primary text-primary-foreground shadow-xs'
                : 'text-muted-foreground hover:text-foreground hover:bg-muted'
            }`}
          >
            <Send className="w-4 h-4" />
            <span>Sent ({sent.length})</span>
          </button>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Fetching invitations..." />
      ) : error ? (
        <ErrorMessage message={error} onRetry={loadInvitations} />
      ) : currentList.length === 0 ? (
        <EmptyState
          title={tab === 'inbox' ? 'Your inbox is clear' : 'No outgoing invitations'}
          description={
            tab === 'inbox'
              ? 'When other team leaders invite you to join their team, their invitations will appear here.'
              : 'Use the AI Teammate Finder to send invitations to compatible students.'
          }
          icon={Mail}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {currentList.map((inv) => (
            <InvitationCard
              key={inv.invitationId}
              invitation={inv}
              type={tab}
              onRespond={handleRespond}
              onWithdraw={handleWithdraw}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Invitations;
