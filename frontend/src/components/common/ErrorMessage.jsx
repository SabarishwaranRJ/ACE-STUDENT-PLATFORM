import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';

const ErrorMessage = ({ message = 'Something went wrong', onRetry }) => {
  return (
    <div className="p-4 bg-destructive/10 border border-destructive/20 rounded-[var(--radius)] text-destructive flex items-start space-x-3 my-4">
      <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-destructive" />
      <div className="flex-1 text-sm font-medium">{message}</div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-xs bg-destructive/20 hover:bg-destructive/30 text-destructive px-3 py-1 rounded-[calc(var(--radius)-0.25rem)] flex items-center space-x-1 transition font-medium"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Retry</span>
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;
