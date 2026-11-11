import React from 'react';
import { Loader2 } from 'lucide-react';

const LoadingSpinner = ({ text = 'Loading...', size = 'md' }) => {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
      <Loader2 className={`${sizeMap[size] || sizeMap.md} animate-spin text-primary mb-3`} />
      <span className="text-sm font-medium">{text}</span>
    </div>
  );
};

export default LoadingSpinner;
