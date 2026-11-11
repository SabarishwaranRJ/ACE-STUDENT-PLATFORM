import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { teamsApi } from '../api/teams';
import TeamMemberCard from '../components/teams/TeamMemberCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import Modal from '../components/common/Modal';
import { Users, Plus, Wrench, Sparkles, LogOut, Layers } from 'lucide-react';

const TeamDetail = () => {
  const { teamId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [team, setTeam] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Modals
  const [isAddReqOpen, setIsAddReqOpen] = useState(false);
  const [newReq, setNewReq] = useState({
    requiredSkillId: 1,
    minimumProficiency: 'Intermediate',
  });

  const loadTeam = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await teamsApi.getTeam(teamId);
      if (res.success) {
        setTeam(res.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to load team');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTeam();
  }, [teamId]);

  const isCurrentUserLeader = team?.leader?.studentId === user?.studentId;

  const handleApproveMember = async (studentId) => {
    try {
      await teamsApi.approveMember(teamId, studentId);
      loadTeam();
    } catch (err) {
      alert(err.message || 'Failed to approve');
    }
  };

  const handleRemoveMember = async (studentId) => {
    if (!window.confirm('Remove this member?')) return;
    try {
      await teamsApi.removeMember(teamId, studentId);
      loadTeam();
    } catch (err) {
      alert(err.message || 'Failed to remove');
    }
  };

  const handleLeaveTeam = async () => {
    if (!window.confirm('Are you sure you want to leave this team?')) return;
    try {
      await teamsApi.leaveTeam(teamId);
      navigate('/teams');
    } catch (err) {
      alert(err.message || 'Failed to leave team');
    }
  };

  const handleAddRequirement = async (e) => {
    e.preventDefault();
    try {
      await teamsApi.addRequirement(teamId, newReq);
      setIsAddReqOpen(false);
      loadTeam();
    } catch (err) {
      alert(err.message || 'Failed to add requirement');
    }
  };

  const handleRemoveRequirement = async (reqId) => {
    try {
      await teamsApi.removeRequirement(teamId, reqId);
      loadTeam();
    } catch (err) {
      alert(err.message || 'Failed to remove requirement');
    }
  };

  if (loading) return <LoadingSpinner text="Loading team dashboard..." />;
  if (error) return <ErrorMessage message={error} onRetry={loadTeam} />;

  const activeMembers = team?.members?.filter(m => m.membershipStatus === 'Active') || [];
  const pendingMembers = team?.members?.filter(m => m.membershipStatus === 'Pending') || [];

  return (
    <div className="space-y-6">
      {/* Team Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center space-x-2.5 mb-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold border border-primary/30 bg-primary/10 text-primary">
              {team?.teamStatus}
            </span>
            <span className="text-xs text-muted-foreground font-semibold">•</span>
            <span className="text-xs font-bold text-primary">
              {team?.event?.eventName}
            </span>
          </div>

          <h1 className="text-3xl font-black text-foreground tracking-tight">{team?.teamName}</h1>
          <p className="text-sm text-muted-foreground mt-2 leading-relaxed max-w-2xl">
            {team?.description || 'No team description.'}
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2.5">
          <Link
            to={`/teams/${teamId}/workspace`}
            className="flex items-center space-x-1.5 px-4 py-2.5 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition shadow-xs"
          >
            <Users className="w-4 h-4" />
            <span>Open Team Workspace</span>
          </Link>

          <Link
            to={`/builder?teamId=${teamId}&eventId=${team?.event?.eventId}`}
            className="flex items-center space-x-1.5 px-3.5 py-2.5 bg-accent hover:opacity-90 text-accent-foreground border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition"
          >
            <Wrench className="w-4 h-4" />
            <span>AI Team Builder</span>
          </Link>

          {!isCurrentUserLeader && (
            <button
              onClick={handleLeaveTeam}
              className="flex items-center space-x-1.5 px-4 py-2.5 bg-destructive/10 hover:bg-destructive/20 text-destructive border border-destructive/20 rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition"
            >
              <LogOut className="w-4 h-4" />
              <span>Leave Team</span>
            </button>
          )}
        </div>
      </div>

      {/* Grid: Roster & Requirements */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Active Members Roster */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2">
                <Users className="w-4 h-4 text-primary" />
                <h3 className="text-sm font-bold text-foreground uppercase tracking-wider">
                  Active Team Members ({activeMembers.length} / {team?.event?.requiredTeamSize || 5})
                </h3>
              </div>
            </div>

            <div className="space-y-2.5">
              {activeMembers.map((member) => (
                <TeamMemberCard
                  key={member.studentId}
                  member={member}
                  isLeader={member.studentId === team?.leader?.studentId}
                  isCurrentUserLeader={isCurrentUserLeader}
                  onRemove={handleRemoveMember}
                />
              ))}
            </div>
          </div>

          {/* Pending Applications (Leader only) */}
          {isCurrentUserLeader && pendingMembers.length > 0 && (
            <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
              <h3 className="text-sm font-bold text-foreground uppercase tracking-wider mb-4 text-primary">
                Pending Join Requests ({pendingMembers.length})
              </h3>
              <div className="space-y-2.5">
                {pendingMembers.map((member) => (
                  <TeamMemberCard
                    key={member.studentId}
                    member={member}
                    isLeader={false}
                    isCurrentUserLeader={true}
                    onApprove={handleApproveMember}
                    onRemove={handleRemoveMember}
                  />
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Team Requirements & Recruitment Spec */}
        <div className="space-y-6">
          <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2">
                <Layers className="w-4 h-4 text-primary" />
                <h3 className="text-sm font-bold text-foreground uppercase tracking-wider">
                  Required Skills
                </h3>
              </div>
              {isCurrentUserLeader && (
                <button
                  onClick={() => setIsAddReqOpen(true)}
                  className="flex items-center space-x-1 text-xs font-bold text-primary hover:underline"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Add Skill</span>
                </button>
              )}
            </div>

            <div className="space-y-2">
              {team?.requirements && team.requirements.length > 0 ? (
                team.requirements.map((req) => (
                  <div
                    key={req.requirementId}
                    className="flex items-center justify-between p-3 rounded-[calc(var(--radius)-0.125rem)] bg-muted border border-border"
                  >
                    <div>
                      <span className="text-xs font-bold text-foreground block">
                        {req.skill?.skillName}
                      </span>
                      <span className="text-[11px] text-muted-foreground">
                        Min. {req.minimumProficiency}
                      </span>
                    </div>
                    {isCurrentUserLeader && (
                      <button
                        onClick={() => handleRemoveRequirement(req.requirementId)}
                        className="text-xs text-destructive hover:underline font-semibold"
                      >
                        Remove
                      </button>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-xs text-muted-foreground italic">
                  No required skills posted yet. Add skills to help the AI matching engine recommend optimal teammates!
                </p>
              )}
            </div>

            <div className="mt-6 pt-6 border-t border-border">
              <Link
                to={`/recommendations?eventId=${team?.event?.eventId}`}
                className="w-full flex items-center justify-center space-x-1.5 py-2.5 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition shadow-xs"
              >
                <Sparkles className="w-4 h-4" />
                <span>Search Candidates</span>
              </Link>
            </div>
          </div>
        </div>

      </div>

      {/* Add Skill Requirement Modal */}
      <Modal isOpen={isAddReqOpen} onClose={() => setIsAddReqOpen(false)} title="Add Team Skill Requirement">
        <form onSubmit={handleAddRequirement} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-1">
              Select Required Skill
            </label>
            <select
              value={newReq.requiredSkillId}
              onChange={(e) => setNewReq({ ...newReq, requiredSkillId: Number(e.target.value) })}
              className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value={1} className="bg-card text-foreground">Python</option>
              <option value={2} className="bg-card text-foreground">React</option>
              <option value={3} className="bg-card text-foreground">Machine Learning</option>
              <option value={4} className="bg-card text-foreground">SQL</option>
              <option value={5} className="bg-card text-foreground">Java</option>
              <option value={6} className="bg-card text-foreground">Docker</option>
              <option value={7} className="bg-card text-foreground">UI/UX Design</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-1">
              Minimum Proficiency Level
            </label>
            <select
              value={newReq.minimumProficiency}
              onChange={(e) => setNewReq({ ...newReq, minimumProficiency: e.target.value })}
              className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="Beginner" className="bg-card text-foreground">Beginner</option>
              <option value="Intermediate" className="bg-card text-foreground">Intermediate</option>
              <option value="Advanced" className="bg-card text-foreground">Advanced</option>
              <option value="Expert" className="bg-card text-foreground">Expert</option>
            </select>
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-border">
            <button
              type="button"
              onClick={() => setIsAddReqOpen(false)}
              className="px-4 py-2 border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold text-muted-foreground hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold"
            >
              Save Requirement
            </button>
          </div>
        </form>
      </Modal>

    </div>
  );
};

export default TeamDetail;
