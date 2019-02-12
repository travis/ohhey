import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";

import * as queries from './queries';
import Comments from './Comments'

export const Claim = ({claim}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const {id, body, agreeCount, disagreeCount, supportCount, opposeCount} = claim
  return (
    <div style={{border: "1px dotted black"}}>
      <h5>{body}</h5>
      a: {agreeCount} d: {disagreeCount} s: {supportCount} o: {opposeCount}

      <Comments claim={claim}/>
      <button onClick={() => setShowEvidence(!evidenceShown)}>{evidenceShown ? "Hide" : "Show"} Evidence</button>
      {evidenceShown && (
        <EvidenceList claimID={id}/>
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

const EvidenceList = graphql(
  queries.EvidenceForClaim, {
    props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
  }
)(({claim, evidenceList}) => (
  <Fragment>
    {evidenceList && evidenceList.map((evidence) => (
      <Evidence evidence={evidence}/>
    ))}
  </Fragment>
))

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
