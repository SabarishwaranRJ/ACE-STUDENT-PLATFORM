import React from 'react';
import { Check, X, Clock, Users, ArrowUpRight, Ban } from 'lucide-react';

const InvitationCard = ({ invitation, type = 'inbox', onRespond, onWithdraw }) => {
  const isInbox = type === 'inbox';
  const team = invitation.team;
  const isPending = invitation.invitationStatus === 'Pending';

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Accepted':
        return 'bg-emerald-500/10 text-emerald-600 border-emerald-500/30';
      case 'Declined':
        return 'bg-red-500/10 text-destructive border-destructive/30';
      case 'Withdrawn':
        return 'bg-muted text-muted-foreground border-border';
      default:
        return 'bg-primary/10 text-primary border-primary/30';
    }
  };

  return (
    <div className="p-5 bg-card rounded-[var(--radius)] border border-border shadow-xs hover:border-primary/50 transition flex flex-col justify-between">
      <div>
        <div className="flex items-center justify-between mb-3">
          <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${getStatusBadge(invitation.invitationStatus)}`}>
            {invitation.invitationStatus}
          </span>
          <span className="text-xs text-muted-foreground">
            {new Date(invitation.createdAt).toLocaleDateString()}
          </span>
        </div>

        <h4 className="text-base font-bold text-foreground mb-1">{team?.teamName || 'Team Invitation'}</h4>
        <p className="text-xs font-semibold text-primary mb-3">{team?.event?.eventName || 'Hackathon Event'}</p>

        <p className="text-xs text-muted-foreground mb-4">
          {isInbox ? (
            <>Invited by <span className="font-semibold text-foreground">{invitation.sender?.name}</span> to join their team.</>
          ) : (
            <>Sent to <span className="font-semibold text-foreground">{invitation.receiver?.name}</span> for recruitment.</>
          )}
        </p>
      </div>

      {/* Actions */}
      <div className="pt-3 border-t border-border flex items-center justify-end space-x-2">
        {isInbox && isPending && (
          <>
            <button
              onClick={() => onRespond(invitation.invitationId, false)}
              className="px-3.5 py-1.5 rounded-[calc(var(--radius)-0.4rem)] text-xs font-bold text-muted-foreground hover:bg-muted transition"
            >
              Decline
            </button>
            <button
              onClick={() => onRespond(invitation.invitationId, true)}
              className="flex items-center space-x-1 px-4 py-1.5 bg-primary hover:bg-[#1a8cd8] text-primary-foreground rounded-[calc(var(--radius)-0.4rem)] text-xs font-bold transition shadow-xs"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Accept & Join Team</span>
            </button>
          </>
        )}

        {!isInbox && isPending && (
          <button
            onClick={() => onWithdraw(invitation.invitationId)}
            className="flex items-center space-x-1 px-3 py-1.5 rounded-[calc(var(--radius)-0.4rem)] text-xs font-bold text-destructive hover:bg-destructive/10 transition"
          >
            <Ban className="w-3.5 h-3.5" />
            <span>Withdraw</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default InvitationCard;
