import React, { useState } from 'react';
import CompatibilityScore from '../common/CompatibilityScore';
import SkillBadge from '../common/SkillBadge';
import { ThumbsUp, ThumbsDown, Send, Sparkles } from 'lucide-react';

const TeammateCard = ({ candidate, onInvite, onFeedback, onViewProfile }) => {
  const [feedbackSent, setFeedbackSent] = useState(null);

  const handleFeedback = (type) => {
    setFeedbackSent(type);
    if (onFeedback) {
      onFeedback(candidate.recommendedStudentId, type);
    }
  };

  return (
    <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs hover:border-primary/50 transition flex flex-col justify-between">
      <div>
        {/* Header: Name, Role & Match Score */}
        <div className="flex items-start justify-between gap-3 mb-3">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 rounded-[calc(var(--radius)-0.125rem)] bg-primary text-primary-foreground font-black text-lg flex items-center justify-center shadow-xs">
              {candidate.name?.charAt(0) || 'S'}
            </div>
            <div>
              <h3 className="font-bold text-foreground text-base leading-snug">{candidate.name}</h3>
              <p className="text-xs font-semibold text-primary">
                {candidate.preferredRole || 'Team Player'}
              </p>
            </div>
          </div>
          <CompatibilityScore score={candidate.compatibilityScore} />
        </div>

        {/* AI Explanation Callout */}
        {candidate.explanation && (
          <div className="p-3 bg-accent text-accent-foreground rounded-[calc(var(--radius)-0.125rem)] border border-border text-xs leading-relaxed mb-4 flex items-start space-x-2">
            <Sparkles className="w-4 h-4 text-primary flex-shrink-0 mt-0.5" />
            <span>{candidate.explanation}</span>
          </div>
        )}

        {/* Complementary Skills */}
        {candidate.complementarySkills && candidate.complementarySkills.length > 0 && (
          <div className="mb-3">
            <span className="text-[11px] font-bold text-muted-foreground uppercase tracking-wider block mb-1.5">
              Complementary Skills
            </span>
            <div className="flex flex-wrap gap-1.5">
              {candidate.complementarySkills.map((s, idx) => (
                <SkillBadge key={idx} skillName={s} isCovered={true} />
              ))}
            </div>
          </div>
        )}

        {/* Matching Skills */}
        {candidate.skillMatches && candidate.skillMatches.length > 0 && (
          <div className="mb-4">
            <span className="text-[11px] font-bold text-muted-foreground uppercase tracking-wider block mb-1.5">
              Matching Skills
            </span>
            <div className="flex flex-wrap gap-1.5">
              {candidate.skillMatches.map((s, idx) => (
                <SkillBadge key={idx} skillName={s} />
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Action Footer */}
      <div className="pt-4 border-t border-border flex items-center justify-between">
        {/* Feedback Buttons */}
        <div className="flex items-center space-x-1">
          <button
            onClick={() => handleFeedback('Like')}
            title="Helpful recommendation"
            className={`p-1.5 rounded-[calc(var(--radius)-0.25rem)] border transition ${
              feedbackSent === 'Like'
                ? 'bg-emerald-500/20 text-emerald-600 border-emerald-500/30'
                : 'text-muted-foreground hover:text-foreground hover:bg-muted border-transparent'
            }`}
          >
            <ThumbsUp className="w-4 h-4" />
          </button>
          <button
            onClick={() => handleFeedback('Dislike')}
            title="Not a good fit"
            className={`p-1.5 rounded-[calc(var(--radius)-0.25rem)] border transition ${
              feedbackSent === 'Dislike'
                ? 'bg-destructive/20 text-destructive border-destructive/30'
                : 'text-muted-foreground hover:text-destructive hover:bg-muted border-transparent'
            }`}
          >
            <ThumbsDown className="w-4 h-4" />
          </button>
        </div>

        <div className="flex items-center space-x-2">
          {onViewProfile && (
            <button
              onClick={() => onViewProfile(candidate.recommendedStudentId)}
              className="px-3 py-1.5 text-xs font-bold text-muted-foreground hover:text-foreground hover:bg-muted rounded-[calc(var(--radius)-0.125rem)] transition"
            >
              View Profile
            </button>
          )}
          <button
            onClick={() => onInvite(candidate)}
            className="flex items-center space-x-1 px-3.5 py-1.5 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition shadow-xs"
          >
            <Send className="w-3.5 h-3.5" />
            <span>Invite</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default TeammateCard;
