import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { Button, Box, Layer } from 'grommet'
import {
  Add, Like, Dislike, Chat, ChatOption, AddCircle, SubtractCircle, New, Close,
  WifiNone, Wifi, WifiLow, WifiMedium
} from "grommet-icons";

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
  const {id, body, creator, agreeCount, disagreeCount, supportCount, opposeCount, agree, disagree, score} = claim
  return (
    <Fragment>
      <Box
        justify="center"
        align="center"
        pad="small"
        border={{ color: 'brand', size: 'small' }}
        round="medium"
        width="50em"
      >
        <h3>{body}</h3>
        <p>by {creator.username}</p>
        <p>agree: {agreeCount} disagree: {disagreeCount} supporting: {supportCount} opposing: {opposeCount}</p>
        <h4>{score}</h4>
        <div>
          <Button primary={agree} icon={<Like />} label="Agree" onClick={() => vote(true)}/>
          <Button primary={disagree} icon={<Dislike />} label="Disagree" onClick={() => vote(false)}/>
        </div>
        <Button icon={<Chat/>} onClick={() => setShowComments(!commentsShown)}>{commentsShown ? "Hide" : "Show"} Comments</Button>
        {commentsShown && (
          <Layer full="vertical" position="right">
            <Box fill style={{ minWidth: "378px" }}>
              <Box
                direction="row"
                align="center"
                as="header"
                elevation="small"
                justify="between">
                <h3>Comments on {body}</h3>
                <Button icon={<Close/>} onClick={() => setShowComments(false)}></Button>
              </Box>
              <Box flex overflow="auto" pad="xsmall">
                <Comments claim={claim}/>
              </Box>
            </Box>
          </Layer>
        )}
        <Button icon={<ChatOption/>}
                label={`${evidenceShown ? "Hide" : "Show"} Evidence`}
                onClick={() => setShowEvidence(!evidenceShown)}/>
      </Box>
      {evidenceShown && (
        <EvidenceList claim={claim}/>
      )}
    </Fragment>
  )
})


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
      <Button label="0%"
              primary={myRelevanceRating === 0}
              onClick={() => relevanceVote(0)}/>
      <Button label="33%"
              primary={myRelevanceRating === 33}
              onClick={() => relevanceVote(33)}/>
      <Button label="66%"
              primary={myRelevanceRating === 66}
              onClick={() => relevanceVote(66)}/>
      <Button label="100%"
              primary={myRelevanceRating === 100}
              onClick={() => relevanceVote(100)}/>

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
      <Button
        icon={<New/>}
        label="Create Argument"
        type="submit"/>
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
    <Box margin={{left: "2em"}} border={{ color: 'brand', size: 'medium' }}>
      {evidenceList && evidenceList.map((evidence) => (
        <Evidence evidence={evidence} key={evidence.id}/>
      ))}
      <Button icon={<AddCircle/>}
              label="Add Supporting Argument"
        onClick={() => setEvidenceAdder("support")}/>
      <Button icon={<SubtractCircle/>}
              label="Add Counter Argument"
        onClick={() => setEvidenceAdder("oppose")}/>
      {evidenceAdder && (
        <EvidenceAdder supports={evidenceAdder == 'support'} claim={claim}/>
      )}
    </Box>
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
