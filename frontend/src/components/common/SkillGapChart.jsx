import React from 'react';
import { CheckCircle2, AlertTriangle, Layers } from 'lucide-react';

const SkillGapChart = ({ presentSkills = [], missingSkills = [], coveragePercent = 0 }) => {
  return (
    <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-2">
          <Layers className="w-5 h-5 text-primary" />
          <h4 className="font-bold text-foreground">Team Skill Coverage Analysis</h4>
        </div>
        <span className="text-lg font-black text-primary">{Math.round(coveragePercent)}%</span>
      </div>

      {/* Progress Bar */}
      <div className="w-full bg-muted rounded-full h-3 mb-6 overflow-hidden">
        <div
          className="bg-primary h-3 rounded-full transition-all duration-500 ease-out"
          style={{ width: `${Math.min(100, Math.max(0, coveragePercent))}%` }}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Covered Skills */}
        <div className="p-4 bg-primary/10 rounded-[calc(var(--radius)-0.125rem)] border border-primary/20">
          <div className="flex items-center space-x-1.5 text-primary text-xs font-bold uppercase tracking-wider mb-2">
            <CheckCircle2 className="w-4 h-4 text-primary" />
            <span>Covered by Team ({presentSkills.length})</span>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {presentSkills.length > 0 ? (
              presentSkills.map((s, idx) => (
                <span key={idx} className="px-2.5 py-0.5 rounded-md text-xs font-semibold bg-primary/20 text-primary">
                  {s}
                </span>
              ))
            ) : (
              <span className="text-xs text-muted-foreground italic">No skills registered yet</span>
            )}
          </div>
        </div>

        {/* Missing Skills */}
        <div className="p-4 bg-amber-500/10 rounded-[calc(var(--radius)-0.125rem)] border border-amber-500/20">
          <div className="flex items-center space-x-1.5 text-amber-600 text-xs font-bold uppercase tracking-wider mb-2">
            <AlertTriangle className="w-4 h-4 text-amber-500" />
            <span>Missing / In-Demand ({missingSkills.length})</span>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {missingSkills.length > 0 ? (
              missingSkills.map((s, idx) => (
                <span key={idx} className="px-2.5 py-0.5 rounded-md text-xs font-semibold bg-amber-500/20 text-amber-600">
                  {s}
                </span>
              ))
            ) : (
              <span className="text-xs text-primary font-medium">All required skills are covered!</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SkillGapChart;
