import React from 'react';

export const StopPropagation = ({children}) => (
  <span onClick={(e) => e.stopPropagation()}>
    {children}
  </span>
)
