import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { Route, Switch, withRouter } from "react-router-dom";
import { withStyles } from '@material-ui/core/styles';
import {
  ClaimToolbar, EvidenceExpansionPanel, EvidenceExpansionPanelSummary, EvidenceExpansionPanelDetails,
  ClaimIntroType
} from './claim'
import {
  ClaimPaper, ClaimBody, Typography, Box, Link
} from './ui'
import { ExpandMoreIcon } from './icons'
import * as queries from '../queries';
import { userPrefix } from '../urls'

const RoutePrefixSwitch = ({believes, doesntbelieve, isntsureif, fallback}) => (
  <Switch>
    {believes &&  <Route path={`/${userPrefix}/:username/believes/:slug`}><Fragment>{believes}</Fragment></Route>}
    {doesntbelieve && <Route path={`/${userPrefix}/:username/doesntbelieve/:slug`}><Fragment>{doesntbelieve}</Fragment></Route>}
    {isntsureif && <Route path={`/${userPrefix}/:username/isntsureif/:slug`}><Fragment>{isntsureif}</Fragment></Route>}
    {fallback && <Route path={`/${userPrefix}/:username`}><Fragment>{fallback}</Fragment></Route>}
  </Switch>
)


export const ClaimBodyLink = ({username, claim: {slug, body}}) => (
  <Link to={`/somesay/${slug}`}>{body}</Link>
)

const Evidence = compose(
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
    },
    relevance: {
      marginLeft: "1em"
    }
  }))
)(({classes, relevanceVote, evidence: {id, supports, claim, relevance, myRelevanceRating}, username}) => {
  const [expanded, setExpanded] = useState(false)
  return (
    <div key={id}>
      <EvidenceExpansionPanel onChange={(e, expanded) => setExpanded(expanded)}>
        <EvidenceExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="subtitle2">
            <ClaimBodyLink claim={claim}/>
          </Typography>
          {relevance && (
            <Typography className={classes.relevance} variant="caption">
              ({relevance}% relevant)
            </Typography>
          )}
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
    {list && list.filter((evidence) => (evidence.supports === support)).map((evidence) => (
      <Evidence evidence={evidence} username={username} key={evidence.id}/>
    ))}
  </Fragment>
)

const EvidenceList = ({claim, username, evidence, support,
                       placeholder, sentimentMap, nested}) => {
  return (
    <Box mt={2}>
      <Box display="flex">
        <Typography variant={nested? "h6" : "h5"} fontFamily="claimBody">
          <RoutePrefixSwitch {...sentimentMap} />
        </Typography>
      </Box>
      <Evidences claim={claim} list={evidence} support={support}/>
    </Box>
  )
}

const SupportList = ({claim, username, evidence}) => (
  <EvidenceList claim={claim} username={username}
                support={true} placeholder="why?"
                evidence={evidence}
                sentimentMap={{
                  believes: "because",
                  doesntbelieve: "despite",
                  isntsureif: "on one hand",
                  fallback :"on one hand"
                }}/>

)

const OpposeList = ({claim, username, evidence}) => (
  <EvidenceList claim={claim} username={username}
                support={false} placeholder="why not?"
                evidence={evidence}
                sentimentMap={{
                  believes: "despite",
                  doesntbelieve: "because",
                  isntsureif: "but on the other",
                  fallback :"but on the other"
                }}/>
)

const EvidenceLists = graphql(
  queries.UserEvidenceForClaim, {
    options: ({username, claim}) => ({
      variables: {username, claimID: claim.id}
    }),
    props: ({data: {userEvidenceForClaim}}) => ({evidenceList: userEvidenceForClaim})
  }
)(({claim, username, evidenceList, nested, ...props}) => {
  const Support = () => (evidenceList && (evidenceList.length > 0) && (
    <SupportList claim={claim} username={username} evidence={evidenceList} nested={nested}/>
  )) || ""
  const Oppose = () => (evidenceList && (evidenceList.length > 0) && (
    <OpposeList claim={claim} username={username} evidence={evidenceList} nested={nested}/>
  )) || ""
  return (
    <div {...props}>
      <RoutePrefixSwitch
        doesntbelieve={<Fragment><Oppose/><Support/></Fragment>}
        fallback={<Fragment><Support/><Oppose/></Fragment>}
      />
    </div>
  )
})

export default withRouter(({history, username, claim}) => {
  return (
    <ClaimPaper>
      <ClaimToolbar claim={claim} />
      <ClaimIntroType>
        @{username}
        <RoutePrefixSwitch
          believes=" believes"
          doesntbelieve=" doesn't believe"
          isntsureif=" isn't sure whether"
        />
      </ClaimIntroType>
      <ClaimBody>
        <ClaimBodyLink username={username} claim={claim}/>
      </ClaimBody>
      <EvidenceLists username={username} claim={claim}/>
    </ClaimPaper>
  )
})
