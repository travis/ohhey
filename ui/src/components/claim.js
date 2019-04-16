import React, {Fragment, useState} from 'react';
import { compose } from "react-apollo";
import { styled } from '@material-ui/core/styles';
import { withRouter } from "react-router-dom";

import { ExpandMoreIcon } from './icons'
import * as goto from '../goto';
import { withAuth } from '../authentication'

import {
  Typography, Button, Drawer, List, ListItem, ListItemText, IconButton, Toolbar,
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails
} from './ui'
import { Chat, Close, Info, Person } from './icons'

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

const ClaimToolbarButton = styled(Button)({})

export const ClaimToolbar = compose(
  withRouter,
  withAuth
)(({history, currentUser, claim}) => {
  const [commentsShown, setShowComments] = useState(false)
  const [infoShown, setShowInfo] = useState(false)
  const { body, creator } = claim
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
        {creator && (
          <Typography variant="caption" color="textSecondary" align="center">
            created by {creator.username}
          </Typography>
        )}
        <ClaimScore claim={claim}/>
        <p>Created at {new Date(claim.createdAt).toString()}</p>
      </Drawer>
    </Fragment>
  )
})

export const EvidenceExpansionPanel = styled(
  (props) => (<ExpansionPanel elevation={0} {...props}/>)
)({
  backgroundColor: "transparent",
  '&:before': {
    display: "none"
  }
})


export const EvidenceExpansionPanelSummary = styled(
  (props) => <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>} {...props}/>
)({
  width: '100%'
})

export const EvidenceExpansionPanelDetails = ExpansionPanelDetails

export const ClaimIntroType = (props) => (
  <Typography variant="subtitle1" align="center" {...props} />
)

export const EvidenceClaimBodyType = (props) => (
  <Typography variant="h6" fontFamily="claimBody" {...props}/>
)
