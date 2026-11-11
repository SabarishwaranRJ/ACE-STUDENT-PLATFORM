import React from 'react';

const SkillBadge = ({ skillName, proficiency, isMissing = false, isCovered = false }) => {
  const getProfColor = () => {
    if (isMissing) return 'bg-amber-500/10 text-amber-600 border-amber-500/30';
    if (isCovered) return 'bg-primary/10 text-primary border-primary/30';
    switch (proficiency?.toLowerCase()) {
      case 'expert':
        return 'bg-purple-500/10 text-purple-600 border-purple-500/30';
      case 'advanced':
        return 'bg-primary/15 text-primary border-primary/30';
      case 'intermediate':
        return 'bg-accent text-accent-foreground border-border';
      default:
        return 'bg-muted text-muted-foreground border-border';
    }
  };

  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-[calc(var(--radius)-0.25rem)] text-xs font-semibold border ${getProfColor()}`}>
      <span>{skillName}</span>
      {proficiency && (
        <span className="text-[10px] opacity-80 font-normal tracking-wide uppercase">
          • {proficiency}
        </span>
      )}
    </span>
  );
};

export default SkillBadge;
