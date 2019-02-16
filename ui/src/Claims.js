import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import Link from "./Link";
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import Fab from '@material-ui/core/Fab';
import Drawer from '@material-ui/core/Drawer';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Chat from '@material-ui/icons/Chat';
import Like from '@material-ui/icons/ThumbUp';
import Dislike from '@material-ui/icons/ThumbDown';
import Close from '@material-ui/icons/Close';
import Forum from '@material-ui/icons/Forum';
import Create from '@material-ui/icons/Create';
import Add from '@material-ui/icons/Add';
import SubtractCircle from '@material-ui/icons/RemoveCircle';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';


import {Form, TextArea} from './form'

import * as queries from './queries';
import Comments from './Comments'

export const Claim = graphql(
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
)(({claim, vote}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const [commentsShown, setShowComments] = useState(false)
  const {id, body, slug, creator, agreeCount, disagreeCount, supportCount, opposeCount, agree, disagree, score} = claim
  return (
    <Paper>
        <Typography variant="h5" color="textPrimary"><Link to={`/ibelieve/${slug}`}>{body}</Link></Typography>
        <Typography variant="caption" color="textSecondary">created by {creator.username}</Typography>
        <p>agree: {agreeCount} disagree: {disagreeCount} supporting: {supportCount} opposing: {opposeCount}</p>
        <h4>{score}</h4>
        <div>
          <Button color={agree ? 'secondary' : 'default'} onClick={() => vote(true)}>
            Agree
          </Button>
          <Button color={disagree ? 'secondary' : 'default'} onClick={() => vote(false)}>
            Disagree
          </Button>
        </div>
        <IconButton onClick={() => setShowComments(!commentsShown)}>
          <Chat/>
        </IconButton>
        <Drawer open={commentsShown} anchor="right" onClose={() => setShowComments(false)}>
          <IconButton onClick={() => setShowComments(false)}><Close/></IconButton>
          <h3>Comments on {body}</h3>
          <Comments claim={claim}/>
        </Drawer>
        <Button variant="contained" onClick={() => setShowEvidence(!evidenceShown)}>
          <Forum/>{evidenceShown ? "Hide" : "Show"} Evidence
        </Button>
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
  const color = supports ? 'blue' : 'red'
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
  const color = supports ? 'blue' : 'red'
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
    {list && list.filter((evidence) => console.log(evidence) || (evidence.supports == support)).map((evidence) => (
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
