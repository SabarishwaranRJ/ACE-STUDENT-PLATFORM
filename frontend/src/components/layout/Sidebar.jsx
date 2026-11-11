import React from 'react';
import { NavLink } from 'react-router-dom';
import { Calendar, Users, Sparkles, Mail, User, PlusCircle, Wrench } from 'lucide-react';

const Sidebar = ({ isOpen, onClose }) => {
  const navItems = [
    { name: 'Events', path: '/events', icon: Calendar },
    { name: 'AI Teammate Finder', path: '/recommendations', icon: Sparkles },
    { name: 'AI Team Builder', path: '/builder', icon: Wrench },
    { name: 'My Teams', path: '/teams', icon: Users },
    { name: 'Create Team', path: '/teams/create', icon: PlusCircle },
    { name: 'Invitations', path: '/invitations', icon: Mail },
    { name: 'My Profile', path: '/profile', icon: User },
  ];

  return (
    <>
      {isOpen && (
        <div
          onClick={onClose}
          className="fixed inset-0 bg-slate-900/60 z-30 md:hidden backdrop-blur-xs"
        />
      )}

      <aside
        className={`fixed md:sticky top-16 left-0 z-30 w-64 h-[calc(100vh-4rem)] bg-card border-r border-border p-4 transition-transform duration-200 ease-in-out overflow-y-auto ${
          isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        <div className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3.5 py-2.5 rounded-[var(--radius)] text-sm font-semibold transition ${
                    isActive
                      ? 'bg-accent text-accent-foreground font-bold'
                      : 'text-muted-foreground hover:text-foreground hover:bg-muted'
                  }`
                }
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                <span>{item.name}</span>
              </NavLink>
            );
          })}
        </div>

        {/* AI Engine Status Badge */}
        <div className="mt-8 p-3.5 bg-accent/60 rounded-[var(--radius)] border border-border text-xs">
          <div className="flex items-center space-x-1.5 text-primary font-bold mb-1">
            <span className="w-2 h-2 rounded-full bg-primary animate-pulse" />
            <span>AI Matching Engine</span>
          </div>
          <p className="text-muted-foreground text-[11px] leading-relaxed">
            Sentence-BERT & OR-Tools integer optimization online.
          </p>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
