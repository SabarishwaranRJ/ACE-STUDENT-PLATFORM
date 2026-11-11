import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { teamsApi } from '../api/teams';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import { Users, Plus, ArrowRight, ShieldCheck, Calendar } from 'lucide-react';

const Teams = () => {
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadTeams = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await teamsApi.getMyTeams();
      if (res.success) {
        setTeams(res.data || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to load teams');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTeams();
  }, []);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-accent text-accent-foreground border border-border mb-3">
            <Users className="w-3.5 h-3.5" />
            <span>My Active Teams</span>
          </div>
          <h1 className="text-3xl font-black text-foreground tracking-tight">Team Hub</h1>
          <p className="text-sm text-muted-foreground mt-1">Manage team rosters, recruitment, and required skill profiles.</p>
        </div>

        <Link
          to="/teams/create"
          className="flex items-center space-x-1.5 px-4 py-2.5 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition shadow-sm"
        >
          <Plus className="w-4 h-4" />
          <span>New Team</span>
        </Link>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading team rosters..." />
      ) : error ? (
        <ErrorMessage message={error} onRetry={loadTeams} />
      ) : teams.length === 0 ? (
        <EmptyState
          title="You have not joined any teams"
          description="Create your own team or discover teammates using the AI Teammate Finder."
          actionText="Create a Team"
          actionLink="/teams/create"
          icon={Users}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {teams.map((team) => (
            <div
              key={team.teamId}
              className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs hover:border-primary/50 transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-bold border border-primary/30 bg-primary/10 text-primary">
                    {team.teamStatus}
                  </span>
                  <div className="flex items-center space-x-1 text-muted-foreground text-xs font-semibold">
                    <Users className="w-4 h-4 text-muted-foreground" />
                    <span>{team.memberCount || 1} Members</span>
                  </div>
                </div>

                <h3 className="text-lg font-bold text-foreground mb-1">{team.teamName}</h3>
                <p className="text-xs font-semibold text-primary mb-3">
                  {team.event?.eventName || 'Hackathon Event'}
                </p>

                <p className="text-sm text-muted-foreground line-clamp-2 mb-4 leading-relaxed">
                  {team.description || 'No description provided.'}
                </p>
              </div>

              <div className="pt-4 border-t border-border flex items-center justify-between gap-2">
                <Link
                  to={`/teams/${team.teamId}`}
                  className="text-xs font-semibold text-muted-foreground hover:text-foreground"
                >
                  Roster
                </Link>
                <Link
                  to={`/teams/${team.teamId}/workspace`}
                  className="inline-flex items-center space-x-1 px-3 py-1.5 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
                >
                  <span>Team Workspace</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Teams;
