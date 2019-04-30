import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
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

import * as goto from '../goto';
import * as queries from '../queries';

export const ClaimBodyLink = ({username, claim: {slug, body}}) => (
  <Link to={`/somesay/${slug}`}>{body}</Link>
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

const Evidences = ({list, username, support}) => (
  <Fragment>
    {list.map((evidence) => (
      <Evidence evidence={evidence} username={username} key={evidence.id}/>
    ))}
  </Fragment>
)

const evidenceIntroText = (claim, sentimentMap) => {
  if (claim) {
    switch(claim.userMeta && claim.userMeta.agreement) {
    case undefined:
      return ""
    case null:
      return ""
    case 100:
      return sentimentMap.believes
    case -100:
      return sentimentMap.doesntbelieve
    default:
      return sentimentMap.isntsureif
    }
  }
}

const EvidenceList = ({claim, username, evidence, support, placeholder, sentimentMap, nested}) => {
  const myEvidence = evidence && evidence.filter(e => e.supports === support)
  return (myEvidence && (myEvidence.length > 0)) ? (
    <Box mt={2}>
      <Box display="flex">
        <Typography variant={nested? "h6" : "h5"} fontFamily="claimBody">
          {evidenceIntroText(claim, sentimentMap)}
        </Typography>
      </Box>
      <Evidences claim={claim} list={myEvidence} support={support} username={username}/>
    </Box>
  ) : ""
}

const SupportList = (props) => (
  <EvidenceList support={true} placeholder="why?"
                sentimentMap={{
                  believes: "because",
                  doesntbelieve: "despite",
                  isntsureif: "on one hand",
                }}
                {...props}/>
)

const OpposeList = (props) => (
  <EvidenceList support={false} placeholder="why not?"
                sentimentMap={{
                  believes: "despite",
                  doesntbelieve: "because",
                  isntsureif: "but on the other",
                }}
                {...props}/>
)

const EvidenceLists = graphql(
  queries.UserEvidenceForClaim, {
    options: ({username, claim}) => ({
      variables: {username, claimID: claim.id}
    }),
    props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
  }
)(({claim, username, evidenceList, nested, ...props}) => {
  const agreement = claim && claim.userMeta && claim.userMeta.agreement
  const Support = () => (evidenceList && (evidenceList.length > 0) && (
    <SupportList claim={claim} username={username} evidence={evidenceList} nested={nested}/>
  )) || ""
  const Oppose = () => (evidenceList && (evidenceList.length > 0) && (
    <OpposeList claim={claim} username={username} evidence={evidenceList} nested={nested}/>
  )) || ""
  return (
    <div {...props}>
      {(agreement === -100) ?
         (<Fragment><Oppose/><Support/></Fragment>) :
         (<Fragment><Support/><Oppose/></Fragment>)
      }
    </div>
  )
})

const introText = (claim) => {
  const agreement = claim && claim.userMeta && claim.userMeta.agreement
  switch(agreement) {
  case undefined:
    return ""
  case null:
    return ""
  case 100:
    return " believes "
  case -100:
    return " doesn't believe "
  default:
    return " isn't sure whether "
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
        {introText(claim)}
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
