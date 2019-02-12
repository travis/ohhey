import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";

import {Form, Text} from './form'

import * as queries from './queries';
import Comments from './Comments'

export const Claim = ({claim}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const [commentsShown, setShowComments] = useState(false)
  const {id, body, agreeCount, disagreeCount, supportCount, opposeCount} = claim
  return (
    <div style={{border: "1px dotted black"}}>
      <h5>{body}</h5>
      <p>a: {agreeCount} d: {disagreeCount} s: {supportCount} o: {opposeCount}</p>
      <button onClick={() => setShowComments(!commentsShown)}>{commentsShown ? "Hide" : "Show"} Comments</button>
      {commentsShown && (<Comments claim={claim}/>)}
      <button onClick={() => setShowEvidence(!evidenceShown)}>{evidenceShown ? "Hide" : "Show"} Evidence</button>
      {evidenceShown && (
        <EvidenceList claim={claim}/>
      )}
    </div>
  )
}


const Evidence = ({evidence: {id, supports, claim, relevance}}) => {
  const color = supports ? 'blue' : 'red'
  return (
    <div key={id} style={{color, border: `1px solid ${color}`}}>
      {relevance} % relevant
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
      <Text field="body" placeholder="Make an argument!"/>
      <button type="submit">Create Argument</button>
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
    <Fragment>
      {evidenceList && evidenceList.map((evidence) => (
        <Evidence evidence={evidence} key={evidence.id}/>
      ))}
      <button onClick={() => setEvidenceAdder("support")}>{evidenceAdder == 'support' ? "Hide" : "Show"} Add Supporting Argument</button>
      <button onClick={() => setEvidenceAdder("oppose")}>{evidenceAdder == 'oppose' ? "Hide" : "Show"} Add Counter Argument</button>
      {evidenceAdder && (
        <EvidenceAdder supports={evidenceAdder == 'support'} claim={claim}/>
      )}
    </Fragment>
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
