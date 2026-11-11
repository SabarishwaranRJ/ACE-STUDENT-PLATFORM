import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { eventsApi } from '../api/events';
import { teamsApi } from '../api/teams';
import { Users, AlertCircle, ArrowRight } from 'lucide-react';

const CreateTeam = () => {
  const [searchParams] = useSearchParams();
  const preselectedEventId = searchParams.get('eventId');

  const [events, setEvents] = useState([]);
  const [formData, setFormData] = useState({
    teamName: '',
    eventId: preselectedEventId || '',
    description: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const res = await eventsApi.getAllEvents();
        if (res.success && res.data.length > 0) {
          setEvents(res.data);
          if (!formData.eventId) {
            setFormData(prev => ({ ...prev, eventId: String(res.data[0].eventId) }));
          }
        }
      } catch (err) {
        console.error(err);
      }
    };
    fetchEvents();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await teamsApi.createTeam({
        teamName: formData.teamName,
        eventId: Number(formData.eventId),
        description: formData.description,
      });

      if (res.success && res.data) {
        navigate(`/teams/${res.data.teamId}`);
      }
    } catch (err) {
      setError(err.message || 'Failed to create team');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-4">
      <div className="bg-card rounded-[var(--radius)] border border-border p-8 shadow-xs">
        
        <div className="mb-8">
          <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-accent text-primary border border-border mb-3">
            <Users className="w-3.5 h-3.5" />
            <span>Form a New Team</span>
          </div>
          <h1 className="text-2xl font-black text-foreground tracking-tight">Create Team</h1>
          <p className="text-sm text-slate-500 mt-1">
            As the creator, you will become the Team Leader with permissions to recruit and approve teammates.
          </p>
        </div>

        {error && (
          <div className="p-3.5 bg-red-500/10 border border-destructive/20 rounded-[calc(var(--radius)-0.4rem)] text-destructive text-sm flex items-center space-x-2 mb-6">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-2">
              Select Event *
            </label>
            <select
              value={formData.eventId}
              onChange={(e) => setFormData({ ...formData, eventId: e.target.value })}
              required
              className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.4rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            >
              {events.map((ev) => (
                <option key={ev.eventId} value={ev.eventId} className="bg-card text-foreground">
                  {ev.eventName} (Team of {ev.requiredTeamSize || 4})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-2">
              Team Name *
            </label>
            <input
              type="text"
              required
              value={formData.teamName}
              onChange={(e) => setFormData({ ...formData, teamName: e.target.value })}
              placeholder="e.g. AlgoRhythms, NeuralKnights"
              className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.4rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-foreground uppercase tracking-wider mb-2">
              Team Vision / Description
            </label>
            <textarea
              rows={4}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Describe your project idea and what specific skills you want in teammates..."
              className="w-full px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.4rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-primary hover:bg-[#1a8cd8] text-primary-foreground rounded-[var(--radius)] font-bold text-sm transition shadow-sm flex items-center justify-center space-x-2 disabled:opacity-50 mt-6"
          >
            {loading ? <span>Creating Team...</span> : <span>Launch Team & Start Recruiting</span>}
          </button>
        </form>

      </div>
    </div>
  );
};

export default CreateTeam;
