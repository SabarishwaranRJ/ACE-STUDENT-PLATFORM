import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { eventsApi } from '../api/events';
import { aiApi } from '../api/ai';
import { teamsApi } from '../api/teams';
import { invitationsApi } from '../api/invitations';
import TeammateCard from '../components/teammates/TeammateCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import Modal from '../components/common/Modal';
import { Sparkles, Calendar, Send, CheckCircle2 } from 'lucide-react';

const Recommendations = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialEventId = searchParams.get('eventId');

  const [events, setEvents] = useState([]);
  const [selectedEventId, setSelectedEventId] = useState(initialEventId || '');
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Team Invitation Modal State
  const [myTeams, setMyTeams] = useState([]);
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [selectedTeamId, setSelectedTeamId] = useState('');
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [inviteSuccess, setInviteSuccess] = useState('');

  // Load events list on mount
  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const res = await eventsApi.getAllEvents();
        if (res.success && res.data.length > 0) {
          setEvents(res.data);
          if (!selectedEventId) {
            setSelectedEventId(String(res.data[0].eventId));
          }
        }
      } catch (err) {
        console.error('Failed to load events', err);
      }
    };

    fetchEvents();
  }, []);

  // Fetch recommendations whenever selectedEventId changes
  const loadRecommendations = async () => {
    if (!selectedEventId) return;
    setLoading(true);
    setError('');
    try {
      const res = await aiApi.getRecommendations(selectedEventId, 10);
      if (res.success) {
        setRecommendations(res.data || []);
      }
    } catch (err) {
      setError(err.message || 'AI Matching currently unavailable');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedEventId) {
      setSearchParams({ eventId: selectedEventId });
      loadRecommendations();
    }
  }, [selectedEventId]);

  // Load teams when invite modal is triggered
  const handleOpenInvite = async (candidate) => {
    setSelectedCandidate(candidate);
    setIsInviteModalOpen(true);
    setInviteSuccess('');
    try {
      const res = await teamsApi.getMyTeams();
      if (res.success) {
        const eligibleTeams = (res.data || []).filter(
          (t) => String(t.event?.eventId) === String(selectedEventId) && t.teamStatus === 'Open'
        );
        setMyTeams(eligibleTeams);
        if (eligibleTeams.length > 0) {
          setSelectedTeamId(String(eligibleTeams[0].teamId));
        }
      }
    } catch (err) {
      console.error('Failed to load user teams', err);
    }
  };

  const handleSendInvite = async (e) => {
    e.preventDefault();
    if (!selectedTeamId || !selectedCandidate) return;

    try {
      const res = await invitationsApi.sendInvitation({
        teamId: Number(selectedTeamId),
        receiverId: selectedCandidate.recommendedStudentId,
      });

      if (res.success) {
        setInviteSuccess(`Invitation sent to ${selectedCandidate.name}!`);
        setTimeout(() => {
          setIsInviteModalOpen(false);
          setInviteSuccess('');
        }, 1800);
      }
    } catch (err) {
      alert(err.message || 'Failed to send invitation');
    }
  };

  const handleFeedback = async (studentId, feedbackType) => {
    try {
      await aiApi.sendFeedback({
        recommendedStudentId: studentId,
        eventId: Number(selectedEventId),
        feedbackType,
      });
    } catch (err) {
      console.error('Failed to record feedback:', err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header & Event Selector */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="max-w-xl">
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-accent text-accent-foreground border border-border mb-3">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Sentence-BERT Compatibility Engine</span>
            </div>
            <h1 className="text-3xl font-black text-foreground tracking-tight">AI Teammate Finder</h1>
            <p className="text-sm text-muted-foreground mt-2 leading-relaxed">
              Our AI evaluates complementary technical skills, availability overlaps, and project interests to suggest teammates who elevate your team's capabilities.
            </p>
          </div>

          {/* Event Picker */}
          <div className="w-full md:w-72">
            <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-2">
              Select Target Event
            </label>
            <div className="relative">
              <Calendar className="w-4 h-4 text-muted-foreground absolute left-3.5 top-3.5" />
              <select
                value={selectedEventId}
                onChange={(e) => setSelectedEventId(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                {events.map((ev) => (
                  <option key={ev.eventId} value={ev.eventId} className="bg-card text-foreground">
                    {ev.eventName}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Recommendations Feed */}
      {loading ? (
        <LoadingSpinner text="Computing SBERT semantic embeddings and compatibility rankings..." />
      ) : error ? (
        <ErrorMessage message={error} onRetry={loadRecommendations} />
      ) : recommendations.length === 0 ? (
        <EmptyState
          title="No recommendations for this event"
          description="Either all eligible candidates are already placed in teams, or no candidates share this event's target domain."
          icon={Sparkles}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {recommendations.map((cand) => (
            <TeammateCard
              key={cand.recommendedStudentId}
              candidate={cand}
              onInvite={handleOpenInvite}
              onFeedback={handleFeedback}
            />
          ))}
        </div>
      )}

      {/* Invite Modal */}
      <Modal
        isOpen={isInviteModalOpen}
        onClose={() => setIsInviteModalOpen(false)}
        title={`Invite ${selectedCandidate?.name || 'Teammate'} to Team`}
      >
        {inviteSuccess ? (
          <div className="text-center py-6 text-primary font-bold flex flex-col items-center space-y-2">
            <CheckCircle2 className="w-10 h-10 text-primary animate-bounce" />
            <span>{inviteSuccess}</span>
          </div>
        ) : myTeams.length === 0 ? (
          <div className="text-center py-6">
            <p className="text-sm text-muted-foreground mb-4">
              You don't currently have an open team registered for this event.
            </p>
            <a
              href={`/teams/create?eventId=${selectedEventId}`}
              className="inline-flex items-center space-x-1.5 px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold"
            >
              Create Team for this Event
            </a>
          </div>
        ) : (
          <form onSubmit={handleSendInvite} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-2">
                Choose Team
              </label>
              <select
                value={selectedTeamId}
                onChange={(e) => setSelectedTeamId(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                {myTeams.map((t) => (
                  <option key={t.teamId} value={t.teamId} className="bg-card text-foreground">
                    {t.teamName} ({t.memberCount} members)
                  </option>
                ))}
              </select>
            </div>

            <div className="flex justify-end space-x-2 pt-4 border-t border-border">
              <button
                type="button"
                onClick={() => setIsInviteModalOpen(false)}
                className="px-4 py-2 border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold text-muted-foreground hover:bg-muted"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold flex items-center space-x-1.5"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Send Invitation</span>
              </button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};

export default Recommendations;
