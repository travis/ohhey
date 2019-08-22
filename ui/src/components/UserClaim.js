import React, { Fragment, useState } from 'react';
import { graphql } from "@apollo/react-hoc";
import { compose } from '../util'
import { withStyles } from '@material-ui/core/styles';
import { withRouter } from "react-router-dom";
import {
  ClaimToolbar, EvidenceExpansionPanel, EvidenceExpansionPanelSummary, EvidenceExpansionPanelDetails,
  ClaimIntroType, EvidenceClaimBodyType, RelevanceBox, AgreeButton, DisagreeButton, NotSureButton
} from './claim'
import {
  ClaimPaper, ClaimBody, Typography, Box, Link
} from './ui'
import { ExpandMoreIcon } from './icons'
import { withAuth } from '../authentication'
import Sources, {Citation} from './Sources'

import * as goto from '../goto';
import * as queries from '../queries';

export const ClaimBodyLink = ({username, claim: {slug, body, sources, quoting}}) => (
  <Fragment>
    {quoting && "“"}
    <Link to={`/somesay/${slug}`}>{body}</Link>
    {quoting && "”"}
    {sources && <Sources sources={sources}/>}
    {quoting && (<Citation source={quoting}/>)}
  </Fragment>
)

const Evidence = compose(
  withAuth,
  graphql(
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
  ),
  withStyles(theme => ({
    evidenceLists: {
      width: '100%'
    }
  }))
)(({classes, relevanceVote, username,
    authData: {currentUser},
    evidence: {id, supports, claim,  myRelevanceRating, userMeta}}) => {
  const [expanded, setExpanded] = useState(false)
  const relevance = userMeta && userMeta.relevance
  return (
    <div key={id}>
      <EvidenceExpansionPanel onChange={(e, expanded) => setExpanded(expanded)}>
        <EvidenceExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          {currentUser && (
            <RelevanceBox>
              {relevance && (<Fragment>{relevance}%</Fragment>)}
            </RelevanceBox>
          )}
          <EvidenceClaimBodyType>
            <ClaimBodyLink claim={claim}/>
          </EvidenceClaimBodyType>
        </EvidenceExpansionPanelSummary>
        <EvidenceExpansionPanelDetails>
          {expanded && (
            <EvidenceLists claim={claim} username={username} className={classes.evidenceLists} nested={true} />
          )}
        </EvidenceExpansionPanelDetails>
      </EvidenceExpansionPanel>
    </div>
  )
})

const Evidences = ({list, username}) => (
  <Fragment>
    {list.map((evidence) => (
      <Evidence evidence={evidence} username={username} key={evidence.id}/>
    ))}
  </Fragment>
)

const introText = ({
  // does this user agree with top level claim?
  // ie, Toby believes cats are cute
  true: {
    // does this evidence support the top level claim?
    // ie Cats are soft
    true: {
      // does this user agree with the evidence?
      true: "because they believe",
      false: "despite not believing"
    },
    // ie Cats are mean
    false: {
      // does this user agree with the evidence?
      true: "despite believing",
      false: "because they don't believe"
    }
  },
  // ie, Toby doesn't believe cats are cute
  false: {
    // does this evidence support the top level claim?
    // ie, Cats are soft
    true: {
      // does this user agree with the evidence?
      true: "despite believing",
      false: "because they don't believe"
    },
    // ie, Cats are mean
    false: {
      // does this user agree with the evidence?
      true: "because they believe",
      false: "despite not believing"
    }
  }
})

const evidenceIntroText = (believesClaim, support, agree) => {
  if ((typeof believesClaim === "boolean") &&
      (typeof support === "boolean") &&
      (typeof agree === "boolean")) {
    return introText[believesClaim][support][agree]
  } else {
    console.error("evidenceIntroText called with ", believesClaim, support, agree)
    return ""
  }
}

const userBelievesClaim = (claim) => claim && claim.userMeta &&
      claim.userMeta.agreement && (claim.userMeta.agreement > 0)

const EvidenceList = ({claim, username, evidence: evidenceList, support, agree, nested}) => {
  const believesClaim = userBelievesClaim(claim)
  const myEvidence = evidenceList && evidenceList.filter(
    (evidence) => (evidence.supports === support) && (userBelievesClaim(evidence.claim) === agree)
  )
  return (myEvidence && (myEvidence.length > 0)) ? (
    <Box mt={2}>
      <Box display="flex">
        <Typography variant={nested? "h6" : "h5"} fontFamily="claimBody">
          {evidenceIntroText(believesClaim, support, agree)}
        </Typography>
      </Box>
      <Evidences claim={claim} list={myEvidence} username={username}/>
    </Box>
  ) : ""
}

const EvidenceLists = graphql(
  queries.UserEvidenceForClaim, {
    options: ({username, claim}) => ({
      variables: {username, claimID: claim.id}
    }),
    props: ({data: {evidenceForClaim}}) => ({evidence: evidenceForClaim})
  }
)(({claim, username, evidence, nested, ...props}) => {
  if (claim && claim.userMeta) {
    const believesClaim = userBelievesClaim(claim)
    const args = {claim, username, evidence, nested}
    return (
      <div {...props}>
        {(believesClaim) ?
         (<Fragment>
            <EvidenceList support={true} agree={true} {...args}/>
            <EvidenceList support={true} agree={false} {...args}/>
            <EvidenceList support={false} agree={true} {...args}/>
            <EvidenceList support={false} agree={false} {...args}/>
          </Fragment>) :
         (<Fragment>
            <EvidenceList support={false} agree={true} {...args}/>
            <EvidenceList support={false} agree={false} {...args}/>
            <EvidenceList support={true} agree={true} {...args}/>
            <EvidenceList support={true} agree={false} {...args}/>
          </Fragment>)
        }
      </div>
    )
  } else {
    return <div>Cannot find user meta for claim.</div>
  }
})


const claimIntroText = (claim) => {
  if (claim && claim.userMeta) {
    const agreement = claim.userMeta.agreement
    if (agreement === 0) {
      return " isn't sure if "
    } else if (agreement > 0) {
      return " believes "
    } else {
      return " doesn't believe "
    }
  } else {
    return ""
  }
}

export default compose(
  withRouter,
  withAuth
)(({authData: {currentUser}, history, username, claim}) => {
  const {myAgreement} = claim;
  return (
    <ClaimPaper>
      <ClaimToolbar claim={claim} isUserClaim />
      <ClaimIntroType>
        @{username}
        {claimIntroText(claim)}
      </ClaimIntroType>
      <ClaimBody>
        <ClaimBodyLink username={username} claim={claim}/>
    </ClaimBody>
      {currentUser && (
        <Typography align="center">
          {(myAgreement !== 100) && (<AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim, 'replace')}/>)}
          {(myAgreement !== 0) && (<NotSureButton claim={claim} onSuccess={(claim) => goto.someSay(history, claim, 'replace')}/>)}
          {(myAgreement !== -100) && (<DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim, 'replace')}/>)}
        </Typography>
      )}
      <EvidenceLists username={username} claim={claim}/>
    </ClaimPaper>
  )
})
