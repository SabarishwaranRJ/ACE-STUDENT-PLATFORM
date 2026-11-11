import React from 'react';
import { Sparkles } from 'lucide-react';

const CompatibilityScore = ({ score }) => {
  const rounded = Math.round(score);

  return (
    <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-sm font-bold border border-primary/30 bg-primary/10 text-primary shadow-xs">
      <Sparkles className="w-4 h-4 text-primary" />
      <span>{rounded}% Match</span>
    </div>
  );
};

export default CompatibilityScore;
