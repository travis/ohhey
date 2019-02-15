import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { Link } from "react-router-dom";
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import Fab from '@material-ui/core/Fab';
import Chat from '@material-ui/icons/Chat';
import Like from '@material-ui/icons/ThumbUp';
import Dislike from '@material-ui/icons/ThumbDown';
import Close from '@material-ui/icons/Close';
import Forum from '@material-ui/icons/Forum';
import Create from '@material-ui/icons/Create';
import AddCircle from '@material-ui/icons/AddCircle';
import SubtractCircle from '@material-ui/icons/RemoveCircle';


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
    <Fragment>
      <Paper>
        <Link to={`/ibelieve/${slug}`}><h3>{body}</h3></Link>
        <p>by {creator.username}</p>
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
        {commentsShown && (
          <Paper>
            <h3>Comments on {body}</h3>
            <IconButton onClick={() => setShowComments(false)}><Close/></IconButton>
            <Comments claim={claim}/>
          </Paper>
        )}
        <Button variant="contained" onClick={() => setShowEvidence(!evidenceShown)}>
          <Forum/>{evidenceShown ? "Hide" : "Show"} Evidence
        </Button>
      </Paper>
      {evidenceShown && (
        <EvidenceList claim={claim}/>
      )}
    </Fragment>
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
    <div key={id} style={{color, border: `1px solid ${color}`}}>
      <p>{relevance} % relevant</p>
      {(myRelevanceRating !== null) && (<p>my vote: {myRelevanceRating}</p>)}
      <RelevanceButton relevance={0} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
      <RelevanceButton relevance={33} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
      <RelevanceButton relevance={66} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
      <RelevanceButton relevance={100} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
      <Claim claim={claim} key={claim.id}/>
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
    <Form onSubmit={addEvidence} style={{color, border: `1px solid ${color}`}}>
      <TextArea field="body" placeholder="Make an argument!"/>
      <Button type="submit">
        <Create/>Create Argument
      </Button>
    </Form>
  )
})

const EvidenceList = graphql(
  queries.EvidenceForClaim, {
    options: ({claim}) => ({
      variables: {claimID: claim.id}
    }),
    props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
  }
)(({claim, evidenceList}) => {
  const [evidenceAdder, setEvidenceAdder] = useState(null)

  return (
    <Paper>
      {evidenceList && evidenceList.map((evidence) => (
        <Evidence evidence={evidence} key={evidence.id}/>
      ))}
      <Button onClick={() => setEvidenceAdder("support")}>
        <AddCircle/> Add Supporting Argument
      </Button>
      <Button onClick={() => setEvidenceAdder("oppose")}>
        <SubtractCircle/> Add Counter Argument
      </Button>
      {evidenceAdder && (
        <EvidenceAdder supports={evidenceAdder == 'support'} claim={claim}/>
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
