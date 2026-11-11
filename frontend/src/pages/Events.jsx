import React, { useState, useEffect } from 'react';
import { eventsApi } from '../api/events';
import EventCard from '../components/events/EventCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import { Search, Filter, Calendar } from 'lucide-react';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');

  const loadEvents = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await eventsApi.getAllEvents();
      if (res.success) {
        setEvents(res.data || []);
      }
    } catch (err) {
      setError(err.message || 'Failed to load events');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEvents();
  }, []);

  const filteredEvents = events.filter((ev) => {
    const matchesSearch =
      ev.eventName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (ev.description && ev.description.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesType = selectedType === 'ALL' || ev.eventType === selectedType;
    return matchesSearch && matchesType;
  });

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs">
        <div className="max-w-2xl">
          <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full text-xs font-bold bg-accent text-accent-foreground border border-border mb-3">
            <Calendar className="w-3.5 h-3.5" />
            <span>Active Hackathons & Competitions</span>
          </div>
          <h1 className="text-3xl font-black text-foreground tracking-tight">Explore Events</h1>
          <p className="text-sm text-muted-foreground mt-2 leading-relaxed">
            Discover hackathons, datathons, and innovation challenges. Find high-synergy teammates powered by Sentence-BERT AI matching.
          </p>
        </div>

        {/* Search & Filter Toolbar */}
        <div className="mt-6 flex flex-col sm:flex-row items-center gap-3">
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 text-muted-foreground absolute left-3.5 top-3.5" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search hackathons by keyword, stack, or title..."
              className="w-full pl-10 pr-4 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring transition"
            />
          </div>

          <div className="flex items-center space-x-2 w-full sm:w-auto">
            <Filter className="w-4 h-4 text-muted-foreground hidden sm:block" />
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="w-full sm:w-auto px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="ALL" className="bg-card text-foreground">All Categories</option>
              <option value="Hackathon" className="bg-card text-foreground">Hackathons</option>
              <option value="Datathon" className="bg-card text-foreground">Datathons</option>
              <option value="Case_Study" className="bg-card text-foreground">Case Studies</option>
              <option value="Project" className="bg-card text-foreground">Projects</option>
            </select>
          </div>
        </div>
      </div>

      {/* Events List */}
      {loading ? (
        <LoadingSpinner text="Fetching open hackathons..." />
      ) : error ? (
        <ErrorMessage message={error} onRetry={loadEvents} />
      ) : filteredEvents.length === 0 ? (
        <EmptyState
          title="No events found"
          description="Try broadening your search term or select 'All Categories'."
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredEvents.map((ev) => (
            <EventCard key={ev.eventId} event={ev} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Events;
