import React from 'react';
import { Crown, Check, X, UserX, Clock } from 'lucide-react';

const TeamMemberCard = ({ member, isLeader, isCurrentUserLeader, onApprove, onRemove }) => {
  const isPending = member.membershipStatus === 'Pending';

  return (
    <div className="flex items-center justify-between p-3.5 rounded-[calc(var(--radius)-0.125rem)] border border-border bg-card hover:border-primary/40 transition">
      <div className="flex items-center space-x-3">
        <div className="w-10 h-10 rounded-[calc(var(--radius)-0.25rem)] bg-muted text-foreground font-bold flex items-center justify-center text-sm">
          {member.name?.charAt(0) || 'U'}
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <h4 className="text-sm font-bold text-foreground">{member.name}</h4>
            {isLeader && (
              <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-600 border border-amber-500/30">
                <Crown className="w-3 h-3 text-amber-500" />
                <span>Leader</span>
              </span>
            )}
            {isPending && (
              <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/15 text-primary border border-primary/30">
                <Clock className="w-3 h-3 text-primary" />
                <span>Pending Approval</span>
              </span>
            )}
          </div>
          <p className="text-xs text-muted-foreground">{member.assignedRole || 'Member'}</p>
        </div>
      </div>

      {/* Leader Actions */}
      {isCurrentUserLeader && !isLeader && (
        <div className="flex items-center space-x-1.5">
          {isPending ? (
            <>
              <button
                onClick={() => onApprove(member.studentId)}
                title="Approve join request"
                className="p-1.5 bg-emerald-500/10 text-emerald-600 hover:bg-emerald-500/20 rounded-md transition"
              >
                <Check className="w-4 h-4" />
              </button>
              <button
                onClick={() => onRemove(member.studentId)}
                title="Decline join request"
                className="p-1.5 bg-destructive/10 text-destructive hover:bg-destructive/20 rounded-md transition"
              >
                <X className="w-4 h-4" />
              </button>
            </>
          ) : (
            <button
              onClick={() => onRemove(member.studentId)}
              title="Remove member from team"
              className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-md transition"
            >
              <UserX className="w-4 h-4" />
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default TeamMemberCard;
