import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Users, Clock, Sparkles } from 'lucide-react';

const EventCard = ({ event }) => {
  return (
    <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs hover:border-primary/50 transition flex flex-col justify-between">
      <div>
        <div className="flex items-center justify-between mb-3">
          <span className="px-2.5 py-0.5 rounded-full text-xs font-bold border border-primary/30 bg-primary/10 text-primary">
            {event.eventType?.replace('_', ' ')}
          </span>
          <div className="flex items-center space-x-1 text-muted-foreground text-xs font-semibold">
            <Users className="w-4 h-4 text-muted-foreground" />
            <span>Team of {event.requiredTeamSize || 4}</span>
          </div>
        </div>

        <h3 className="text-lg font-bold text-foreground mb-2 line-clamp-1">{event.eventName}</h3>
        <p className="text-sm text-muted-foreground line-clamp-3 mb-4 leading-relaxed">
          {event.description || 'No description provided.'}
        </p>
      </div>

      <div className="pt-4 border-t border-border">
        <div className="grid grid-cols-2 gap-2 text-xs text-muted-foreground mb-5">
          <div className="flex items-center space-x-1.5">
            <Calendar className="w-3.5 h-3.5 text-muted-foreground" />
            <span>Event: {event.eventDate || 'TBD'}</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <Clock className="w-3.5 h-3.5 text-muted-foreground" />
            <span>Deadline: {event.registrationDeadline || 'Rolling'}</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Link
            to={`/events/${event.eventId}/teams`}
            className="flex-1 text-center px-3.5 py-2 rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold bg-secondary hover:opacity-90 text-secondary-foreground transition"
          >
            Browse Teams
          </Link>
          <Link
            to={`/recommendations?eventId=${event.eventId}`}
            className="flex-1 flex items-center justify-center space-x-1 px-3.5 py-2 rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold bg-primary hover:opacity-90 text-primary-foreground transition shadow-xs"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Find Teammates</span>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default EventCard;
