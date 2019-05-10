import React, {Fragment, useState} from 'react';
import { withStyles } from '@material-ui/core/styles';

import {Box, ClickAwayListener, Popper, Paper, List, ListItem} from '../components/ui'

export default withStyles(theme => ({
  sup: {
    cursor: "pointer",
    fontSize: "0.67em",
  },
  note: {
    "&:hover": {
      textDecoration: "underline"
    }
  }
}))(({sources, classes}) => {
  const [buttonEl, setShowSources] = useState(null)
  const sourcesShown = Boolean(buttonEl);
  return (
    <ClickAwayListener onClickAway={() => setShowSources(null)}>
      <Box display="inline">
        <sup className={classes.sup} onClick={(e) => setShowSources(buttonEl ? null : e.target)}>
          [<span className={classes.note}>*</span>]
        </sup>
        <Popper open={sourcesShown} onClose={() => setShowSources(null)} anchorEl={buttonEl} >
          <div>
            <Paper>
              <List>
                {sources && sources.map(({url, title}, i) => (
                  <ListItem key={url}>
                    <a href={url} target="_blank" rel="noopener noreferrer">{title}</a>
                  </ListItem>
                ))}
              </List>
            </Paper>
          </div>
        </Popper>
      </Box>
    </ClickAwayListener>
  )
})
