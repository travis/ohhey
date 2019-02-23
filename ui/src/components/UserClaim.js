import React, { Fragment, useState, useEffect } from 'react';
import { graphql, compose } from "react-apollo";
import { Route, Switch, withRouter } from "react-router-dom";
import { withStyles } from '@material-ui/core/styles';

import {
  Paper, Typography, Button, Drawer, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  Link, PopoverButton
} from './ui'
import { ExpandMoreIcon } from './icons'
import {StopPropagation} from './util'
import {RelevanceButton} from './Claims'
import * as queries from '../queries';

const userPrefix = 'someonenamed'
export const believesURL = (username, slug) =>
      `/${userPrefix}/${username}/believes/${slug}`
export const doesntbelieveURL = (username, slug) =>
      `/${userPrefix}/${username}/doesntbelieve/${slug}`
export const isntsureifURL = (username, slug) =>
      `/${userPrefix}/${username}/isntsureif/${slug}`

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
    }
  }))
)(({classes, relevanceVote, evidence: {id, supports, claim, relevance, myRelevanceRating}, username}) => {
  const [expanded, setExpanded] = useState(false)
  return (
    <div key={id}>
      <ExpansionPanel onChange={(e, expanded) => setExpanded(expanded)}>
        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="subtitle2">
            <ClaimBodyLink claim={claim}/>
          </Typography>
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
            <EvidenceLists claim={claim} username={username} className={classes.evidenceLists}/>
          )}
        </ExpansionPanelDetails>
      </ExpansionPanel>
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

const EvidenceList = ({claim, username, evidence, support, placeholder, sentimentMap}) => {
  return (
    <Fragment>
      <Typography variant="h5">
        <RoutePrefixSwitch {...sentimentMap} />
      </Typography>
      <Evidences list={evidence} username={username} support={support}/>
    </Fragment>
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
)(({claim, username, evidenceList, ...props}) => {
  const Support = () => (<SupportList claim={claim} username={username} evidence={evidenceList}/>)
  const Oppose = () => (<OpposeList claim={claim} username={username} evidence={evidenceList}/>)
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
  const {slug, body, agreement, supportCount, opposeCount} = claim
  return (
    <Paper>
      <Typography variant="h5" align="center">
        @{username}
        <RoutePrefixSwitch
          believes=" believes"
          doesntbelieve=" doesn't believe"
          isntsureif=" isn't sure whether"
        />
      </Typography>
      <Typography variant="h4" color="textPrimary" align="center">
        <ClaimBodyLink username={username} claim={claim}/>
      </Typography>
      <EvidenceLists username={username} claim={claim}/>
    </Paper>
  )
})
