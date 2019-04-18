import React, {Fragment, useState} from 'react';
import { withStyles, styled } from '@material-ui/core/styles';
import { withTheme } from '@material-ui/styles';
import { Twitter, Facebook, Reddit } from 'react-social-sharing'
import { CopyToClipboard } from 'react-copy-to-clipboard';

import { ExpandMoreIcon } from './icons'
import * as urls from '../urls';
import { withAuth } from '../authentication'
import { useSnackbar } from 'notistack';
import { baseURL } from '../config'
import { compose } from '../util'



import {
  Box, Typography, Button, Drawer, List, ListItem, ListItemText, IconButton, Toolbar,
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails, Link
} from './ui'
import { Close, Info, Share, Person, People, Link as LinkIcon } from './icons'

//import Comments from './Comments'

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
        <Link to={urls.currentUserView(currentUser, claim)} {...props}>
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

/*
const CommentToolbarButton = ({claim}) => {
  const [commentsShown, setShowComments] = useState(false)
  const {body} = claim
  return (
    <Fragment>
      <ClaimToolbarButton onClick={() => setShowComments(true)}>
        <Chat fontSize="inherit"/>
      </ClaimToolbarButton>
      <Drawer open={commentsShown} anchor="right" onClose={() => setShowComments(false)}>
        <IconButton onClick={() => setShowComments(false)}><Close/></IconButton>
        <h3>Comments on {body}</h3>
        <Comments claim={claim}/>
      </Drawer>
    </Fragment>
  )
}
*/

const InfoToolbarButton = ({claim}) => {
  const [infoShown, setShowInfo] = useState(false)
  const { creator } = claim
  return (
    <Fragment>
      <ClaimToolbarButton onClick={() => setShowInfo(true)}>
        <Info fontSize="inherit"/>
      </ClaimToolbarButton>
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

const ShareToolbarButton = compose(
  withAuth
)(({claim, isUserClaim, authData: {currentUser}}) => {
  const [shareShown, setShowShare] = useState(false)
  const link = `${baseURL}${isUserClaim ? urls.userView(claim) : urls.claim(claim)}`
  const {enqueueSnackbar} = useSnackbar()
  return (
    <Fragment>
      <ClaimToolbarButton onClick={() => setShowShare(true)}>
        <Share fontSize="inherit"/>
      </ClaimToolbarButton>
      <Drawer open={shareShown} anchor="right" onClose={() => setShowShare(false)}>
        <Box display="flex" justifyContent="flex-end">
          <IconButton onClick={() => setShowShare(false)}><Close/></IconButton>
        </Box>
        <Box p={3} textAlign="center" display="flex" flexDirection="column">
          <h4>Share</h4>
          <Typography fontFamily="claimBody" p={2}>{claim.body}</Typography>
          <Box display="flex" justifyContent="space-evenly">
            <Twitter simple link={link}/>
            <Facebook simple link={link}/>
            <Reddit simple link={link}/>
          </Box>
          <CopyToClipboard text={link} onCopy={() => enqueueSnackbar("Copied!")}>
            <Button>
              <LinkIcon/><Typography variant="subtitle2" marginLeft={1}>Copy Link</Typography>
            </Button>
          </CopyToClipboard>
          {currentUser && !isUserClaim && (
            <CopyToClipboard text={`${baseURL}${urls.currentUserView(currentUser, claim)}`}
                             onCopy={() => enqueueSnackbar("Copied!")}>
              <Button>
                <LinkIcon/><Typography variant="subtitle2" marginLeft={1}>Copy Link to My View</Typography>
              </Button>
            </CopyToClipboard>
          )}
        </Box>
      </Drawer>
    </Fragment>
  )
})


export const ClaimToolbar = ({currentUser, claim, isUserClaim}) => {
  return (
    <Fragment>
      <Toolbar position="absolute" mt={-3} left={0} right={0} minHeight={18} px={0.75} justifyContent="space-between">
        <Box>
          <InfoToolbarButton claim={claim}/>
          <Typography variant="caption" align="center" marginRight={1}>{claim.score}</Typography>
        </Box>
        <Box>
          <ViewToggleLink claim={claim} fontSize="button"/>
          {/*<CommentToolbarButton claim={claim}/>*/}
          <ShareToolbarButton claim={claim} isUserClaim={isUserClaim}/>

        </Box>
      </Toolbar>
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

export const RelevanceBox = withTheme(({theme, ...props}) => (
  <Box width={36} position="absolute"
       left={theme.spacing(-2)} top={theme.spacing(1.5)}
       fontWeight={200} fontSize="0.75rem"
       {...props}/>
))
