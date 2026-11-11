import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { teamsApi } from '../api/teams';
import { aiApi } from '../api/ai';
import SkillGapChart from '../components/common/SkillGapChart';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import { Wrench, Sparkles, CheckCircle2, ArrowRight } from 'lucide-react';

const TeamBuilder = () => {
  const [searchParams] = useSearchParams();
  const queryTeamId = searchParams.get('teamId');
  const queryEventId = searchParams.get('eventId');

  const [myTeams, setMyTeams] = useState([]);
  const [selectedTeamId, setSelectedTeamId] = useState(queryTeamId || '');
  const [selectedEventId, setSelectedEventId] = useState(queryEventId || '');
  
  // Analysis State
  const [skillGap, setSkillGap] = useState(null);
  const [optimization, setOptimization] = useState(null);
  const [analyzingGap, setAnalyzingGap] = useState(false);
  const [optimizing, setOptimizing] = useState(false);
  const [error, setError] = useState('');

  // Load user's teams on mount
  useEffect(() => {
    const fetchTeams = async () => {
      try {
        const res = await teamsApi.getMyTeams();
        if (res.success && res.data.length > 0) {
          setMyTeams(res.data);
          if (!selectedTeamId) {
            setSelectedTeamId(String(res.data[0].teamId));
            setSelectedEventId(String(res.data[0].event?.eventId));
          }
        }
      } catch (err) {
        console.error(err);
      }
    };
    fetchTeams();
  }, []);

  // Run Skill Gap Analysis when team is selected
  const runSkillGapAnalysis = async () => {
    if (!selectedTeamId || !selectedEventId) return;
    setAnalyzingGap(true);
    setError('');
    try {
      const res = await aiApi.getSkillGap({
        teamId: Number(selectedTeamId),
        eventId: Number(selectedEventId),
      });
      if (res.success) {
        setSkillGap(res.data);
      }
    } catch (err) {
      setError(err.message || 'Skill gap analysis failed');
    } finally {
      setAnalyzingGap(false);
    }
  };

  useEffect(() => {
    if (selectedTeamId && selectedEventId) {
      runSkillGapAnalysis();
    }
  }, [selectedTeamId, selectedEventId]);

  const handleOptimizeTeam = async () => {
    if (!selectedEventId) return;
    setOptimizing(true);
    try {
      const res = await aiApi.optimizeTeam({
        teamId: Number(selectedTeamId),
        eventId: Number(selectedEventId),
        minTeamSize: 3,
        maxTeamSize: 5,
      });
      if (res.success) {
        setOptimization(res.data);
      }
    } catch (err) {
      alert(err.message || 'Optimization failed');
    } finally {
      setOptimizing(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="max-w-xl">
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-accent text-accent-foreground border border-border mb-3">
              <Wrench className="w-3.5 h-3.5" />
              <span>OR-Tools Integer Programming Engine</span>
            </div>
            <h1 className="text-3xl font-black text-foreground tracking-tight">AI Team Builder</h1>
            <p className="text-sm text-muted-foreground mt-2 leading-relaxed">
              Analyze your current team's technical coverage against event requirements and let the CP-SAT integer optimization solver assemble the optimal roster.
            </p>
          </div>

          {/* Team Selector */}
          {myTeams.length > 0 && (
            <div className="w-full md:w-72">
              <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-2">
                Select Team to Analyze
              </label>
              <select
                value={selectedTeamId}
                onChange={(e) => {
                  setSelectedTeamId(e.target.value);
                  const found = myTeams.find(t => String(t.teamId) === e.target.value);
                  if (found) setSelectedEventId(String(found.event?.eventId));
                }}
                className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                {myTeams.map((t) => (
                  <option key={t.teamId} value={t.teamId} className="bg-card text-foreground">
                    {t.teamName} ({t.event?.eventName})
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>
      </div>

      {error && <ErrorMessage message={error} />}

      {/* Visual Skill Gap Analysis */}
      {analyzingGap ? (
        <LoadingSpinner text="Computing team skill coverage against event requirements..." />
      ) : skillGap ? (
        <div className="space-y-6">
          <SkillGapChart
            presentSkills={skillGap.presentSkills}
            missingSkills={skillGap.missingSkills}
            coveragePercent={skillGap.coveragePercent}
          />

          {/* Recommended Candidate Profiles */}
          {skillGap.candidateMatches && skillGap.candidateMatches.length > 0 && (
            <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                <div className="flex items-center space-x-2">
                  <Sparkles className="w-4 h-4 text-primary" />
                  <h3 className="text-sm font-bold text-foreground uppercase tracking-wider">
                    Recommended Candidates to Fill Gaps
                  </h3>
                </div>
                <button
                  onClick={handleOptimizeTeam}
                  disabled={optimizing}
                  className="flex items-center space-x-1.5 px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition shadow-xs disabled:opacity-50"
                >
                  <Wrench className="w-3.5 h-3.5" />
                  <span>{optimizing ? 'Solving...' : 'Run Full ILP Team Optimization'}</span>
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {skillGap.candidateMatches.map((cand) => (
                  <div
                    key={cand.studentId}
                    className="p-4 bg-muted border border-border rounded-[calc(var(--radius)-0.125rem)] flex flex-col justify-between"
                  >
                    <div>
                      <h4 className="font-bold text-sm text-foreground mb-0.5">{cand.name}</h4>
                      <p className="text-xs font-semibold text-primary mb-2">{cand.role || 'Teammate'}</p>
                      <div className="mb-3">
                        <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider block mb-1">
                          Fills Skill Gaps:
                        </span>
                        <div className="flex flex-wrap gap-1">
                          {cand.fillsSkills.map((sk, idx) => (
                            <span key={idx} className="px-2 py-0.5 bg-primary/10 text-primary border border-primary/20 rounded-md text-xs font-bold">
                              +{sk}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>

                    <a
                      href={`/recommendations?eventId=${selectedEventId}`}
                      className="text-xs font-bold text-primary hover:underline flex items-center space-x-1 pt-2 border-t border-border"
                    >
                      <span>Invite via Finder</span>
                      <ArrowRight className="w-3 h-3" />
                    </a>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Optimal Team Solver Result */}
          {optimization && (
            <div className="p-6 bg-accent text-accent-foreground rounded-[var(--radius)] border border-border">
              <div className="flex items-center space-x-2 text-primary font-bold mb-2">
                <CheckCircle2 className="w-5 h-5 text-primary" />
                <span>Optimal ILP Formulation Found</span>
              </div>
              <p className="text-sm text-foreground mb-4">{optimization.explanation}</p>
              
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {optimization.selectedStudents?.map((s) => (
                  <div key={s.studentId} className="p-3 bg-card border border-border rounded-[calc(var(--radius)-0.125rem)]">
                    <span className="font-bold text-foreground text-sm block">{s.name}</span>
                    <span className="text-xs text-primary">{s.role}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
};

export default TeamBuilder;
