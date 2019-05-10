import React, {Fragment, useState} from 'react';
import { withStyles } from '@material-ui/core/styles';

import {Popper, Paper, List, ListItem} from '../components/ui'

export default withStyles(theme => ({
  sup: {
    fontSize: "0.67em",
  },
  note: {
    cursor: "pointer",
    "&:hover": {
      textDecoration: "underline"
    }
  }
}))(({sources, classes}) => {
  const [buttonEl, setShowSources] = useState(null)
  const sourcesShown = Boolean(buttonEl);

  return (
    <Fragment>
      <sup className={classes.sup} onClick={(e) =>  buttonEl ? setShowSources(null) : setShowSources(e.target)}>
        [<span className={classes.note}>*</span>]
      </sup>
      <Popper open={sourcesShown} onClose={() => setShowSources(null)} anchorEl={buttonEl} >
        <Paper>
          <List>
            {sources && sources.map(({url, title}, i) => (
              <ListItem key={url}>
                <a href={url}>{title}</a>
              </ListItem>
            ))}
          </List>
        </Paper>
      </Popper>
    </Fragment>
  )
})
