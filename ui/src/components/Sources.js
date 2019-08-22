import React, {Fragment, useState} from 'react';
import { withStyles } from '@material-ui/core/styles';

import {Box, Popper, Paper, List, ListItem, NewTabLink} from '../components/ui'

export const Citation = withStyles({
  cite: {
    display: "block",
    fontSize: "0.5em",
    textAlign: "right"
  }
})(({classes, source: {url, title, publication}}) => (
  <cite className={classes.cite}>
    - {publication && (
      <Fragment><NewTabLink href={publication.url}>{publication.name}</NewTabLink>,&nbsp;</Fragment>
    )}
    <NewTabLink href={url}>{title}</NewTabLink>
  </cite>
))

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
      <sup className={classes.sup} onMouseEnter={(e) => buttonEl ? close() : setShowSources(e.target)}>
        [<span className={classes.note}>*</span>]
      </sup>
      <Popper open={sourcesShown} onClose={close} anchorEl={buttonEl} >
        <Paper onMouseLeave={close}>
          <List>
            {sources && sources.map(({url, title, book, publication}, i) => (
              <ListItem key={url || (book && book.url)}>
                <cite>
                  {book ? (
                    <p>
                      <NewTabLink href={book.url}>{book.title}</NewTabLink>
                      {book.author  && (<i className={classes.author}>by {book.author}</i>)}
                    </p>
                  ) : (
                    <Fragment>
                      {publication && (
                        <Fragment><NewTabLink href={publication.url}>{publication.name}</NewTabLink>,&nbsp;</Fragment>
                      )}
                      <NewTabLink href={url}>{title}</NewTabLink>
                    </Fragment>
                  )}
                </cite>
              </ListItem>
            ))}
          </List>
        </Paper>
      </Popper>
    </Box>
  )
})
