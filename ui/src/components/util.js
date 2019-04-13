import React from 'react';
import Box from '@material-ui/core/Box';

export const StopPropagation = ({children, ...rest}) => (
  <Box onClick={(e) => e.stopPropagation()} {...rest}>
    {children}
  </Box>
)
