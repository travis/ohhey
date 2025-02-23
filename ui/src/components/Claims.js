import React, { Fragment, useState } from 'react';
import { compose } from '../util'
import { graphql } from "@apollo/react-hoc";
import { withStyles, styled } from '@material-ui/core/styles';
import { Route, Switch, withRouter } from "react-router-dom";
import { ClaimSpinner, Spinner, ClaimPaper } from './ui'
import {
  ClaimToolbar, EvidenceExpansionPanel, EvidenceExpansionPanelSummary,
  EvidenceExpansionPanelDetails, EvidenceClaimBodyType,
  ClaimIntroType, RelevanceBox, AgreeButton, DisagreeButton, NotSureButton
} from './claim'
import {
  Typography, Button, PopoverButton, Link, Divider, MenuButton, MenuItem, Grid,
  ClaimBody, Box
} from './ui'
import { Form } from './form'
import AutosuggestClaimTextInput from './AutosuggestClaimTextInput'
import Sources, {Citation} from './Sources'
import { StopPropagation } from './util'
import { withAuth } from '../authentication'

import * as goto from '../goto';
import * as queries from '../queries';

const RoutePrefixSwitch = ({ibelieve, idontbelieve, somesay, fallback}) => (
  <Switch>
    {ibelieve &&  <Route path="/ibelieve/:slug"><Fragment>{ibelieve}</Fragment></Route>}
    {idontbelieve && <Route path="/idontbelieve/:slug"><Fragment>{idontbelieve}</Fragment></Route>}
    {somesay && <Route path="/somesay/:slug"><Fragment>{somesay}</Fragment></Route>}
    {fallback && <Route path="/"><Fragment>{fallback}</Fragment></Route>}
  </Switch>
)

export const ClaimBodyLink = ({claim: {slug, body, sources, quoting}}) => (
  <Fragment>
    {quoting && "“"}
    <RoutePrefixSwitch
      ibelieve={<Link to={`/ibelieve/${slug}`}>{body}</Link>}
      idontbelieve={<Link to={`/idontbelieve/${slug}`}>{body}</Link>}
      somesay={<Link to={`/somesay/${slug}`}>{body}</Link>}
      fallback={<Link to={`/somesay/${slug}`}>{body}</Link>}
    />
    {quoting && "”"}
    {sources && <Sources sources={sources}/>}
    {quoting && (<Citation source={quoting}/>)}
  </Fragment>
)


const SentimentMenuButton = styled(MenuButton)({
  textTransform: "inherit",
  fontWeight: "inherit",
  fontSize: "inherit",
  color: "inherit"
})

const SentimentPicker = withRouter(
  ({ history, match: {params: {slug}}}) => (
    <SentimentMenuButton menuItems={[
      <MenuItem key="ibelieve"
                onClick={() => goto.iBelieve(history, {slug}, 'replace')}>
        I believe
      </MenuItem>,
      <MenuItem key="idontbelieve"
                onClick={() => goto.iDontBelieve(history, {slug}, 'replace')}>
        I don't believe
      </MenuItem>,
      <MenuItem key="somesay"
                onClick={() => goto.someSay(history, {slug}, 'replace')}>
        some might say
      </MenuItem>
    ]}>
      <RoutePrefixSwitch
        ibelieve="I believe"
        idontbelieve="I don't believe"
        somesay="some might say"
      />
    </SentimentMenuButton>
  )
)

export const Claim = compose(
  withAuth,
  withRouter,
  withStyles(theme => ({
  }))
)(({authData: {currentUser}, claim, history, classes}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const { myAgreement } = claim

  return (
    <ClaimPaper>
      <ClaimToolbar claim={claim} />
      <ClaimIntroType>
        <RoutePrefixSwitch
          ibelieve={<SentimentPicker>I believe</SentimentPicker>}
          idontbelieve={<SentimentPicker>I don't believe</SentimentPicker>}
          somesay={<SentimentPicker>some might say</SentimentPicker>}
        />
      </ClaimIntroType>
      <ClaimBody>
        <ClaimBodyLink claim={claim}/>
      </ClaimBody>
      {currentUser && (
        <Typography align="center">
          {(myAgreement !== 100) && (<AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim, 'push')}/>)}
          {(myAgreement !== 0) && (<NotSureButton claim={claim} onSuccess={(claim) => goto.someSay(history, claim, 'push')}/>)}
          {(myAgreement !== -100) && (<DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim, 'push')}/>)}
        </Typography>
      )}
      <Typography align="center">
        {!evidenceShown && (
          <Button onClick={() => setShowEvidence(!evidenceShown)}>
            but why?
          </Button>
        )}
      </Typography>
      {evidenceShown && (
        <EvidenceLists claim={claim}/>
      )}
    </ClaimPaper>
  )
})

export const RelevanceButton = ({relevance, myRelevanceRating, relevanceVote}) =>
      <Button color={(myRelevanceRating === relevance) ? 'secondary' : 'default'}
              onClick={() => relevanceVote(relevance)}>
        {relevance}%
      </Button>


export const Evidence = compose(
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
    },
    panel: {
      backgroundColor: "transparent",
      '&:before': {
        display: "none"
      }
    }
  }))
)(({classes, relevanceVote, claim: parentClaim,
    authData: {currentUser},
    evidence: {id, supports, claim, myAgreement, relevance, myRelevanceRating}}) => {
  const [expanded, setExpanded] = useState(false)
  return (
    <EvidenceExpansionPanel onChange={(e, expanded) => setExpanded(expanded)}>
      <EvidenceExpansionPanelSummary>
        {currentUser && (
          <RelevanceBox>
            <StopPropagation>
              <PopoverButton
                px={0} mr={1} minWidth={36} fontWeight="inherit" fontSize="inherit"
                popoverContent={
                  <Box m={2}>
                    <Typography>How relevant is this to "{parentClaim.body}" ?</Typography>
                    <RelevanceButton relevance={0} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                    <RelevanceButton relevance={33} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                    <RelevanceButton relevance={66} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                    <RelevanceButton relevance={100} myRelevanceRating={myRelevanceRating} relevanceVote={relevanceVote}/>
                    {(myRelevanceRating !== null) && (<p>my vote: {myRelevanceRating}</p>)}
                  </Box>
                }>
                {relevance}%
              </PopoverButton>
            </StopPropagation>
          </RelevanceBox>
        )}
        <Box>
          <EvidenceClaimBodyType>
            <ClaimBodyLink claim={claim}/>
          </EvidenceClaimBodyType>
          {currentUser && (
            <StopPropagation>
              {(myAgreement !== 100) && (<AgreeButton claim={claim}/>)}
              {(myAgreement !== 0) && (<NotSureButton claim={claim}/>)}
              {(myAgreement !== -100) && (<DisagreeButton claim={claim}/>)}
            </StopPropagation>
          )}
        </Box>
      </EvidenceExpansionPanelSummary>
      <EvidenceExpansionPanelDetails>
          {expanded && (
            <EvidenceLists claim={claim} className={classes.evidenceLists} nested={true} />
          )}
      </EvidenceExpansionPanelDetails>
    </EvidenceExpansionPanel>
  )
})

const EvidenceAdder = compose(
  graphql(queries.AddEvidence, {
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
          data.evidenceForClaim.unshift(evidence)
          client.writeQuery({...cacheSpec, data})
        }
      })
    })
  })
)(({claim, supports, addEvidence, placeholder}) => {
  const [submitting, setSubmitting] = useState(false)
  const submit = async (values) => {
    setSubmitting(true)
    await addEvidence(values)
    setSubmitting(false)
  }
  return (
    <Form onSubmit={submit}>
      <AutosuggestClaimTextInput
        field="body" placeholder={placeholder}
        fullWidth={true}
        autoComplete="off"
        claimActions={evidenceClaim => submitting ? <Spinner /> : (
          <Button onClick={() => submit({id: evidenceClaim.id})}>add as evidence</Button>
        )}
        onSuggestionSelected={(event, {suggestion: {result: evidenceClaim}}) => {
          event.preventDefault();
          submit({id: evidenceClaim.id})
        }}

        create={query =>
          <Fragment>
            <Divider />
            {submitting ? <Spinner/> : <Button type="submit">add new evidence</Button>}
          </Fragment>
        }/>
    </Form>
  )
})

const Evidences = ({claim, list, support}) => (
  <Fragment>
    {list.map((evidence) => (
      <Evidence claim={claim} evidence={evidence} key={evidence.id}/>
    ))}
  </Fragment>)

export const EvidenceList = withAuth(({
  authData: {currentUser},
  claim, evidence, support, placeholder, sentimentMap, nested}) => {
    const myEvidence = evidence && evidence.filter(e => e.supports === support)
    return (myEvidence && (myEvidence.length > 0)) ? (
      <Box mt={2}>
        <Box display="flex">
          <Typography variant={nested? "h6" : "h5"} fontFamily="claimBody">
            <RoutePrefixSwitch {...sentimentMap} />
          </Typography>
          {currentUser && (
            <Box ml={2} flexGrow={2}>
              <EvidenceAdder supports={support} claim={claim} placeholder={placeholder} />
            </Box>
          )}
        </Box>
        <Evidences claim={claim} list={myEvidence.sort((a, b) => (a.claim.createdAt - b.claim.createdAt))} support={support}/>
      </Box>
    ) : ""
  })


export const SupportList = ({claim, evidence, ...props}) => (
  <EvidenceList claim={claim} support={true} placeholder="why?"
                evidence={evidence}
                sentimentMap={{
                  ibelieve: "because",
                  idontbelieve: "but other people say",
                  somesay: "because",
                  fallback :"because"
                }}
                {...props} />

)

export const OpposeList = ({claim, evidence, ...props}) => (
  <EvidenceList claim={claim} support={false} placeholder="why not?"
                evidence={evidence}
                sentimentMap={{
                  ibelieve: "but other people say",
                  idontbelieve: "because",
                  somesay: "but others say",
                  fallback :"but other people say"
                }}
                {...props} />
)

const EvidenceLists = graphql(
  queries.EvidenceForClaim, {
    options: ({claim}) => ({
      variables: {claimID: claim.id}
    }),
    props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
  }
)(({claim, evidenceList, nested, ...props}) => {
  const Support = () => <SupportList claim={claim} evidence={evidenceList} nested={nested}/>
  const Oppose = () => <OpposeList claim={claim} evidence={evidenceList} nested={nested}/>
  return (
    <Box {...props}>
      <RoutePrefixSwitch
        idontbelieve={<Fragment><Oppose/><Support/></Fragment>}
        fallback={<Fragment><Support/><Oppose/></Fragment>}
      />
    </Box>
  )
})

const ClaimsGrid = ({claims, claimsLoading, claimsError}) => claimsLoading ? (
  <ClaimSpinner/>
) : (claimsError ? (
  <Box textAlign="center">Sorry, something went wrong! Please reload the page.</Box>
) : (
  <Grid container spacing={1}>
    {claims && claims.map((claim) => (
      <Grid item xs={12} key={claim.id}>
        <Claim claim={claim}/>
      </Grid>
    ))}
  </Grid>
))

export default graphql(
  queries.Claims, {
    props: ({data: {claims, loading, error}}) => ({claims, claimsLoading: loading, claimsError: error})
  }
)(ClaimsGrid)
