import React from 'react';

export const StopPropagation = ({children, ...rest}) => (
  <span onClick={(e) => e.stopPropagation()} {...rest}>
    {children}
  </span>
)
