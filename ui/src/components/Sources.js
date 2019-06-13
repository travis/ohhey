import React, {useState} from 'react';
import { withStyles } from '@material-ui/core/styles';

import {Box, Popper, Paper, List, ListItem} from '../components/ui'

export default withStyles(theme => ({
  sup: {
    cursor: "pointer",
    fontSize: "0.67em",
  },
  note: {
    "&:hover": {
      textDecoration: "underline"
    }
  },
  author: {
    display: 'block',
    fontSize: "0.83em"
  }
}))(({sources, classes}) => {
  const [buttonEl, setShowSources] = useState(null)
  const sourcesShown = Boolean(buttonEl);
  const close = () => setShowSources(null)
  return (
    <Box display="inline">
      <sup className={classes.sup} onClick={(e) => buttonEl ? close() : setShowSources(e.target)}>
        [<span className={classes.note}>*</span>]
      </sup>
      <Popper open={sourcesShown} onClose={close} anchorEl={buttonEl} >
        <Paper onMouseLeave={close}>
          <List>
            {sources && sources.map(({url, title, book}, i) => (
              <ListItem key={url || (book && book.url)}>
                {book ?
                 (
                   <p>
                     <a href={book.url} target="_blank" rel="noopener noreferrer">{book.title}</a>
                     {book.author  && (<i className={classes.author}>by {book.author}</i>)}
                   </p>
                 ) :
                 (<a href={url} target="_blank" rel="noopener noreferrer">{title}</a>)}
              </ListItem>
            ))}
          </List>
        </Paper>
      </Popper>
    </Box>
  )
})
