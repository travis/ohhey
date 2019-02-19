import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { withStyles } from '@material-ui/core/styles';

import {
  Paper, Typography, Button, Drawer, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  PopoverButton, List, ListItem, ListItemText, Link, IconButton, Divider, Tooltip
} from './ui'
import { Chat, Close, Create, Add, Remove, ExpandMoreIcon } from './icons'

import {Form, TextInput} from './form'

import * as queries from '../queries';
import Comments from './Comments'
import QuickClaimSearch from './QuickClaimSearch'
import {StopPropagation} from './util'


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

export const ClaimBodyLink = ({claim: {slug, body}}) => (
  <Link to={`/ibelieve/${slug}`}>{body}</Link>
)

const withVote = (Component) => graphql(
  queries.VoteOnClaim, {
    props: ({ ownProps: {claim}, mutate }) => ({
      vote: (agreement) => mutate({
        variables: {
          claimID: claim.id,
          agreement
        }
      })
    })
  }
)(Component)

const agreementButton = (voteValue, text) =>
  withVote(({vote, claim: {myAgreement}, ...props}) => (
    <Button color={(myAgreement === voteValue) ? 'secondary' : 'default'}
            onClick={() => vote(voteValue)}
            {...props}>
      {text}
    </Button>
  ))

export const AgreeButton = agreementButton(100, "I agree")
export const DisagreeButton = agreementButton(-100, "I disagree")
export const NotSureButton = agreementButton(0, "I'm not sure")

export const Claim = compose(
  withStyles(theme => ({
    claimTooltip: {
      backgroundColor: theme.palette.common.white,
      color: 'rgba(0, 0, 0, 0.87)',
      boxShadow: theme.shadows[1],
      fontSize: 11
    }
  }))
)(({claim, classes}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const [commentsShown, setShowComments] = useState(false)
  const {body, creator} = claim

  return (
    <Paper>
      <Typography variant="h5" align="center">
        some people say
      </Typography>
      <Tooltip classes={{tooltip: classes.claimTooltip}} interactive
        title={(
          <Fragment>
            <Typography variant="caption" color="textSecondary" align="center">
              created by {creator.username}
            </Typography>
            <ClaimScore claim={claim}/>
          </Fragment>
        )}>
        <Typography variant="h4" color="textPrimary" align="center">
          <ClaimBodyLink claim={claim}/>
        </Typography>
      </Tooltip>
      <Typography align="center">
        <AgreeButton claim={claim}/>
        <NotSureButton claim={claim}/>
        <DisagreeButton claim={claim}/>
      </Typography>
      <Typography align="center">
        {!evidenceShown && (
          <Button color="primary" onClick={() => setShowEvidence(!evidenceShown)}>
            {evidenceShown ? "oh I see" : "but why?"}
          </Button>
        )}
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
})

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
  const [expanded, setExpanded] = useState(false)
  return (
    <div key={id}>
      <ExpansionPanel onChange={(e, expanded) => setExpanded(expanded)}>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="subtitle2"><Link to={`/ibelieve/${claim.slug}`}>{claim.body}</Link></Typography>
          <Typography variant="caption">
            <StopPropagation>
            <PopoverButton
              popoverContent={
                <Fragment>
                  <RelevanceButton relevance={0} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                  <RelevanceButton relevance={33} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                  <RelevanceButton relevance={66} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                  <RelevanceButton relevance={100} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                  {(myRelevanceRating !== null) && (<p>my vote: {myRelevanceRating}</p>)}
                </Fragment>
              }>
              {relevance}% relevant
            </PopoverButton>
            </StopPropagation>
          </Typography>
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
          {expanded && (
            <EvidenceList claim={claim} style={{width: '100%'}}/>
          )}
        </ExpansionPanelDetails>
      </ExpansionPanel>
    </div>
  )
})

const EvidenceAdder = compose(
  graphql(queries.AddEvidence, {
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
  }),
  withStyles(theme => ({
    bodyInput: {
    }
  }))
)(({claim, supports, addEvidence, placeholder, classes}) => {
  return (
    <Form onSubmit={addEvidence}>
      <TextInput field="body" placeholder={placeholder}
                 fullWidth={true} className={classes.bodyInput} autoComplete="off"/>
      <QuickClaimSearch
        claimActions={claim => (
          <Button>add as evidence</Button>
        )}
        or={
          <Fragment>
            <Divider />
            <Button type="submit">Tell the World!</Button>
          </Fragment>
        }/>
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
)(({claim, evidenceList, ...props}) => {
  const [showSupportAdder, setShowSupportAdder] = useState(false)
  const [showOpposeAdder, setShowOpposeAdder] = useState(false)

  return (
    <div {...props}>
      <Typography variant="h5">
        because
        <IconButton onClick={() => setShowSupportAdder(!showSupportAdder)}>
          {showSupportAdder ? <Remove/> : <Add/> }
        </IconButton>
      </Typography>
      {showSupportAdder && (
        <EvidenceAdder supports={true} claim={claim} placeholder="why?"/>
      )}
      <Evidences list={evidenceList} support={true}/>
      <Typography variant="h5">
        however,
        <IconButton onClick={() => setShowOpposeAdder(!showOpposeAdder)}>
          {showOpposeAdder ? <Remove/> : <Add/> }
        </IconButton>
      </Typography>
      {showOpposeAdder && (
        <EvidenceAdder supports={false} claim={claim} placeholder="why not?"/>
      )}
      <Evidences list={evidenceList} support={false}/>
    </div>
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
