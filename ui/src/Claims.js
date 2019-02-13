import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { Button, Box } from 'grommet'
import { Add, Like, Dislike, Chat, ChatOption, AddCircle, SubtractCircle, New } from "grommet-icons";

import {Form, Text, GrommetText, TextArea} from './form'

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
  const {id, body, creator, agreeCount, disagreeCount, supportCount, opposeCount, agree, disagree} = claim
  return (
    <Fragment>
    <Box
      justify="center"
      align="center"
      pad="small"
      border={{ color: 'brand', size: 'medium' }}
      round="large"
      width="50em"
    >
      <h3>{body}</h3>
      <p>by {creator.username}</p>
      <p>a: {agreeCount} d: {disagreeCount} s: {supportCount} o: {opposeCount}</p>
      <p>{agree && "I AGREE"} {disagree && "I DISAGREE"}</p>
      <div>
        <Button primary={agree} icon={<Like />} label="Agree" onClick={() => vote(true)}/>
        <Button primary={disagree} icon={<Dislike />} label="Disagree" onClick={() => vote(false)}/>
      </div>
      <Button icon={<Chat/>} onClick={() => setShowComments(!commentsShown)}>{commentsShown ? "Hide" : "Show"} Comments</Button>
      {commentsShown && (<Comments claim={claim}/>)}
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


const Evidence = ({evidence: {id, supports, claim, relevance, myRelevanceRating}}) => {
  const color = supports ? 'blue' : 'red'
  return (
    <div key={id} style={{color, border: `1px solid ${color}`}}>
      <p>{relevance} % relevant</p>
      <p>my vote: {myRelevanceRating}</p>

      <Claim claim={claim} key={claim.id}/>
    </div>
  )
}


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
    <div style={{"margin-left": "4em"}} >
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
