import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import Link from "./Link";
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import Drawer from '@material-ui/core/Drawer';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Popover from '@material-ui/core/Popover';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Chat from '@material-ui/icons/Chat';
import Close from '@material-ui/icons/Close';
import Create from '@material-ui/icons/Create';
import Add from '@material-ui/icons/Add';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';


import {Form, TextArea} from './form'

import * as queries from './queries';
import Comments from './Comments'


function ClaimScore({claim}) {
  const {agreement, agreementCount, supportCount, opposeCount, score} = claim
  const [scoreDetailsTarget, setShowScoreDetailsTarget] = useState(false)
  const scoreDetailsShown = Boolean(scoreDetailsTarget);
  return (
    <Typography align="center">
      <Button
        aria-owns={scoreDetailsShown ? 'score-popover' : undefined}
        aria-haspopup="true"
        onClick={(e) => setShowScoreDetailsTarget(e.target)}>
        {score} points
      </Button>
      <Popover
        id="score-popover"
        open={scoreDetailsShown}
        anchorEl={scoreDetailsTarget}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center'
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center'
        }}
        onClose={() => setShowScoreDetailsTarget(null)}>
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
        </List>
      </Popover>
    </Typography>
  )
}

export const ClaimBodyLink = ({claim: {slug, body}}) => (
  <Link to={`/ibelieve/${slug}`}>{body}</Link>
)

const withVote = (Component) => graphql(
  queries.VoteOnClaim, {
    props: ({ ownProps: {claim}, mutate }) => ({
      vote: (agree) => mutate({
        variables: {
          claimID: claim.id,
          agree
        }
      })
    })
  }
)(Component)

const agreementButton = (voteValue) =>
  withVote(({vote, claim: {myAgreement}, ...props}) => (
    <Button color={(myAgreement === voteValue) ? 'secondary' : 'default'}
            onClick={() => vote(voteValue == 100)}
            {...props}>
      {(voteValue == 100) ? "I agree " : "I disagree"}
    </Button>
  ))

export const AgreeButton = agreementButton(100)
export const DisagreeButton = agreementButton(-100)

export const Claim = ({claim}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const [commentsShown, setShowComments] = useState(false)
  const {body, slug, creator, myAgreement} = claim

  return (
    <Paper>
      <Typography variant="h4" color="textPrimary" align="center">
        <ClaimBodyLink claim={claim}/>
      </Typography>
      <Typography variant="caption" color="textSecondary" align="center">
        created by {creator.username}
      </Typography>
      <ClaimScore claim={claim}/>
      <Typography align="center">
        <AgreeButton claim={claim}/>
        <DisagreeButton claim={claim}/>
      </Typography>
      <Typography align="center">
        <Button color="primary" onClick={() => setShowEvidence(!evidenceShown)}>
          {evidenceShown ? "oh I see" : "but why?"}
        </Button>
      </Typography>
      <IconButton onClick={() => setShowComments(!commentsShown)} style={{float: "right", position: "relative", top: "-2em"}}>
        <Chat/>
      </IconButton>
      <Drawer open={commentsShown} anchor="right" onClose={() => setShowComments(false)}>
        <IconButton onClick={() => setShowComments(false)}><Close/></IconButton>
        <h3>Comments on {body}</h3>
        <Comments claim={claim}/>
      </Drawer>
      {evidenceShown && (
        <EvidenceList claim={claim}/>
      )}
    </Paper>
  )
}

const RelevanceButton = ({relevance, myRelevanceRating, relevanceVote}) =>
      <Button color={(myRelevanceRating === relevance) ? 'secondary' : 'default'}
              onClick={() => relevanceVote(relevance)}>
        {relevance}%
      </Button>


const Evidence = graphql(
  queries.VoteOnEvidence, {
    props: ({ ownProps: {evidence}, mutate }) => ({
      relevanceVote: (rating) => mutate({
        variables: {
          evidenceID: evidence.id,
          rating
        }
      })
    })
  }
)(({relevanceVote, evidence: {id, supports, claim, relevance, myRelevanceRating}}) => {
  return (
    <div key={id}>
      <ExpansionPanel>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="subtitle2"><Link to={`/ibelieve/${claim.slug}`}>{claim.body}</Link></Typography>
          <Typography variant="caption">{relevance}% relevant</Typography>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
          {(myRelevanceRating !== null) && (<p>my vote: {myRelevanceRating}</p>)}
          <RelevanceButton relevance={0} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
          <RelevanceButton relevance={33} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
          <RelevanceButton relevance={66} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
          <RelevanceButton relevance={100} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
        </ExpansionPanelDetails>
      </ExpansionPanel>
    </div>
  )
})

const EvidenceAdder = graphql(
  queries.AddEvidence, {
    props: ({ ownProps: {claim, supports}, mutate }) => ({
      addEvidence: (claimInputs) => mutate({
        variables: {
          claimID: claim.id,
          supports,
          claim: claimInputs
        },
        update: (client, {data: {addEvidence: evidence}}) => {
          const cacheSpec = {
            query: queries.EvidenceForClaim,
            variables: { claimID: claim.id }
          };
          const data = client.readQuery(cacheSpec)
          data.evidenceForClaim.push(evidence)
          client.writeQuery({...cacheSpec, data})
        }
      })
    })
  }
)(({claim, supports, addEvidence}) => {
  return (
    <Form onSubmit={addEvidence}>
      <TextArea field="body" placeholder="Make an argument!"/>
      <Button type="submit">
        <Create/>Create Argument
      </Button>
    </Form>
  )
})

const Evidences = ({list, support}) => (
  <Fragment>
    {list && list.filter((evidence) => (evidence.supports === support)).map((evidence) => (
      <Evidence evidence={evidence} key={evidence.id}/>
    ))}
  </Fragment>)

const EvidenceList = graphql(
  queries.EvidenceForClaim, {
    options: ({claim}) => ({
      variables: {claimID: claim.id}
    }),
    props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
  }
)(({claim, evidenceList}) => {
  const [showSupportAdder, setShowSupportAdder] = useState(false)
  const [showOpposeAdder, setShowOpposeAdder] = useState(false)

  return (
    <Paper>
      <Typography variant="subtitle2">people think so because</Typography>
      <Evidences list={evidenceList} support={true}/>
      <Button onClick={() => setShowSupportAdder(true)}>
        <Add/> add more
      </Button>
      {showSupportAdder && (
        <EvidenceAdder supports={true} claim={claim}/>
      )}
      <Typography variant="subtitle2">people are skeptical because</Typography>
      <Evidences list={evidenceList} support={false}/>
      <Button onClick={() => setShowOpposeAdder(true)}>
        <Add/> add more
      </Button>
      {showOpposeAdder && (
        <EvidenceAdder supports={false} claim={claim}/>
      )}
    </Paper>
  )
})

export default graphql(
  queries.Claims, {
    props: ({data: {claims}}) => ({claims})
  }
)(({claims}) => (
  <Fragment>
    {claims && claims.map((claim) => (
      <Claim claim={claim} key={claim.id}/>
    ))}
  </Fragment>
))
