import React, {Fragment, useState} from 'react';
import { compose } from "react-apollo";
import { withStyles } from '@material-ui/core/styles';
import { withRouter } from "react-router-dom";

import * as goto from '../goto';
import { withAuth } from '../authentication'

import {
  Typography, Button, Drawer, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  PopoverButton, List, ListItem, ListItemText, Link, IconButton, Divider, MenuButton, MenuItem, Grid,
  ClaimBody, Box, Toolbar
} from './ui'
import { Chat, Close, ExpandMoreIcon, Info, Person } from './icons'

import Comments from './Comments'

function ClaimScore({claim}) {
  const {agreement, agreementCount, supportCount, opposeCount, score} = claim
  return (
    <List>
      <ListItem>
        <ListItemText primary={`agreement: ${agreement}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`agreement count: ${agreementCount}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`supporting: ${supportCount}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`opposing: ${opposeCount}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`${score} points`} />
      </ListItem>
    </List>
  )
}

const ClaimToolbarButton = withStyles(theme => ({
  root: {
    borderLeftWidth: props => props.noBorder ? 0 : "1px",
    borderLeftStyle: "groove",
    borderLeftColor: theme.palette.text.hint,
    borderRadius: 0,
    minWidth: 0
  },
  label: {
    fontSize: "0.75rem",
    color: theme.palette.text.secondary
  }
}))(({noBorder, ...props}) => <Button {...props}/>)

export const ClaimToolbar = compose(
  withRouter,
  withAuth
)(({history, currentUser, claim}) => {
  const [commentsShown, setShowComments] = useState(false)
  const [infoShown, setShowInfo] = useState(false)
  const {body, creator, myAgreement} = claim
  return (
    <Fragment>
      <Toolbar position="absolute" mt={-3} left={0} right={0} minHeight={18} px={0.75} justifyContent="flex-end">
        <Typography variant="caption" align="center" marginRight={1}>{claim.score}</Typography>
        {claim && currentUser && (
          <ClaimToolbarButton onClick={() => goto.userView(history, currentUser, claim, 'push')}>
            <Person fontSize="inherit"/>
          </ClaimToolbarButton>
        )}
        <ClaimToolbarButton onClick={() => setShowComments(true)}>
          <Chat fontSize="inherit"/>
        </ClaimToolbarButton>
        <ClaimToolbarButton onClick={() => setShowInfo(true)}>
          <Info fontSize="inherit"/>
        </ClaimToolbarButton>
      </Toolbar>
      <Drawer open={commentsShown} anchor="right" onClose={() => setShowComments(false)}>
        <IconButton onClick={() => setShowComments(false)}><Close/></IconButton>
        <h3>Comments on {body}</h3>
        <Comments claim={claim}/>
      </Drawer>
      <Drawer open={infoShown} anchor="left" onClose={() => setShowInfo(false)}>
        <IconButton onClick={() => setShowInfo(false)}><Close/></IconButton>
        <Typography variant="caption" color="textSecondary" align="center">
          created by {creator.username}
        </Typography>
        <ClaimScore claim={claim}/>
        <p>Created at {new Date(claim.createdAt).toString()}</p>
      </Drawer>
    </Fragment>
  )
})
