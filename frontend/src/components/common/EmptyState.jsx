import React from 'react';
import { FolderOpen } from 'lucide-react';
import { Link } from 'react-router-dom';

const EmptyState = ({
  title = 'No items found',
  description = 'Try adjusting your filters or search term.',
  actionText,
  actionLink,
  onAction,
  icon: Icon = FolderOpen
}) => {
  return (
    <div className="text-center py-12 px-4 bg-card rounded-[var(--radius)] border border-border shadow-xs max-w-lg mx-auto">
      <div className="w-14 h-14 bg-muted text-muted-foreground rounded-full flex items-center justify-center mx-auto mb-4">
        <Icon className="w-7 h-7" />
      </div>
      <h3 className="text-lg font-bold text-foreground mb-1">{title}</h3>
      <p className="text-sm text-muted-foreground max-w-sm mx-auto mb-6">{description}</p>
      
      {actionText && actionLink && (
        <Link
          to={actionLink}
          className="inline-flex items-center px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold transition shadow-xs"
        >
          {actionText}
        </Link>
      )}

      {actionText && onAction && (
        <button
          onClick={onAction}
          className="px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-sm font-semibold transition shadow-xs"
        >
          {actionText}
        </button>
      )}
    </div>
  );
};

export default EmptyState;
