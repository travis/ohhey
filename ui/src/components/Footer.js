import React, { useState } from 'react';
import { withStyles } from '@material-ui/core/styles';

import {
  Box, Paper, Button, BottomNavigation, BottomNavigationAction,
  Typography, Dialog
} from '../components/ui'
import {Info} from '../components/icons'
import toby from '../images/toby-info.jpg';

export default withStyles(theme => ({
  footer: {
    position: 'fixed',
    bottom: 0,
    left: 0,
    right: 0
  },
  toby: {
    height: "224px",
    width: "224px",
    marginLeft: 'auto',
    marginRight: 'auto',
    display: 'block',
  },
  content: {
    borderTop: "1px grey solid",
    textAlign: "center",
    '& p, & h5': {
      marginBottom: theme.spacing(1)
    }
  }
}))(({classes}) => {
  const [selected, setSelected] = useState(null)
  const onChange = (_, value) => (value === selected) ? setSelected(null) : setSelected(value)
  return (
    <footer className={classes.footer}>
      <BottomNavigation value={selected} onChange={onChange}>
        <BottomNavigationAction value="info" icon={<Info/>}/>
      </BottomNavigation>
      <Dialog open={selected === "info"}>
        <Paper>
          <Box>
            <img src={toby}
                 alt="Toby Toberson, sock puppet at large"
                 title="Toby Toberson"
                 className={classes.toby} />
          </Box>
          <Box className={classes.content} padding={2}>
            <Typography variant="h5">
              oh hey!
            </Typography>
            <Typography variant="body2">
              Thanks for stopping by!
            </Typography>
            <Typography variant="body2">
              My name is Toby, and this is a project I've been
              working on to help me express myself better. I think self expression
              is really important, and I couldn't find a place on the world wide
              web that welcomed sock puppets, so I made one for myself! This
              page will be updated regularly with stuff I'm thinking about.
              Some of it is stuff I believe, and some is just stuff I wanted to
              think through.
            </Typography>
            <Typography variant="body2">
              If you have any questions or comments, send me an email at
              <a href="mailto:toby@ohhey.fyi"> toby@ohhey.fyi</a> or
              <a href="https://twitter.com/ohheytoby"> tweet at me</a>.
            </Typography>
            <Button size="small" color="primary" onClick={() => setSelected(null)}>
              Cool, thanks!
            </Button>
          </Box>
        </Paper>
      </Dialog>
    </footer>
  )
})
