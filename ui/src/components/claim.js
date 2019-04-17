import React, {Fragment, useState} from 'react';
import { compose } from "react-apollo";
import { withStyles, styled } from '@material-ui/core/styles';
import { withRouter } from "react-router-dom";

import { ExpandMoreIcon } from './icons'
import * as urls from '../urls';
import { withAuth } from '../authentication'

import {
  Box, Typography, Button, Drawer, List, ListItem, ListItemText, IconButton, Toolbar,
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails, Link
} from './ui'
import { Chat, Close, Info, Person, People } from './icons'

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

const ViewToggleLink = withAuth(({authData: {currentUser}, claim, ...props}) => {
  if (claim) {
    if (claim.userMeta) {
      return (
        <Link to={urls.claim(claim)} {...props}>
          <People fontSize="inherit"/>
        </Link>
      )
    } else if (currentUser) {
      return (
        <Link to={urls.userView(currentUser, claim)} {...props}>
          <Person fontSize="inherit"/>
        </Link>
      )
    } else {
      return ""
    }
  } else {
    return ""
  }
})

export const ClaimToolbar = ({currentUser, claim}) => {
  const [commentsShown, setShowComments] = useState(false)
  const [infoShown, setShowInfo] = useState(false)
  const { body, creator } = claim
  return (
    <Fragment>
      <Toolbar position="absolute" mt={-3} left={0} right={0} minHeight={18} px={0.75} justifyContent="space-between">
        <Box>
          <ClaimToolbarButton onClick={() => setShowInfo(true)}>
            <Info fontSize="inherit"/>
          </ClaimToolbarButton>
          <Typography variant="caption" align="center" marginRight={1}>{claim.score}</Typography>
        </Box>
        <Box>
          <ViewToggleLink claim={claim} fontSize="button"/>
          <ClaimToolbarButton onClick={() => setShowComments(true)}>
            <Chat fontSize="inherit"/>
          </ClaimToolbarButton>
        </Box>
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
}

export const EvidenceExpansionPanel = styled(
  (props) => (<ExpansionPanel elevation={0} {...props}/>)
)({
  backgroundColor: "transparent",
  '&:before': {
    display: "none"
  }
})


export const EvidenceExpansionPanelSummary = withStyles({
  root: {
    width: '100%',
  },
  content: {
    alignItems: "center"
  }
})(
  (props) => <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>} {...props}/>
)

export const EvidenceExpansionPanelDetails = ExpansionPanelDetails

export const ClaimIntroType = (props) => (
  <Typography variant="subtitle1" align="center" {...props} />
)

export const EvidenceClaimBodyType = (props) => (
  <Typography variant="h6" fontFamily="claimBody" {...props}/>
)

export const RelevanceBox = (props) => (
  <Box width={36} position="absolute" left={-16} fontWeight={200} fontSize={12}
       {...props}/>
)
