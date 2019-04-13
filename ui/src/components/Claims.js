import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { withStyles } from '@material-ui/core/styles';
import { Route, Switch, withRouter } from "react-router-dom";
import {Spinner, ClaimPaper} from './ui'

import {
  Typography, Button, Drawer, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  PopoverButton, List, ListItem, ListItemText, Link, IconButton, Divider, Tooltip, MenuButton, MenuItem, Grid,
  ClaimBody, Box
} from './ui'
import { Chat, Close, ExpandMoreIcon } from './icons'
import {Form} from './form'
import Comments from './Comments'
import AutosuggestClaimTextInput from './AutosuggestClaimTextInput'
import {StopPropagation} from './util'
import {believesURL, doesntbelieveURL, isntsureifURL} from './UserClaim'
import {withAuth} from '../authentication'
import { claimBodyFont } from '../theme'

import * as goto from '../goto';
import * as queries from '../queries';


function ClaimScore({claim}) {
  const {agreement, agreementCount, supportCount, opposeCount, score} = claim
  return (
    <List>
      <ListItem>
        <ListItemText primary={`agreement: ${agreement}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`agreement count: ${agreementCount}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`supporting: ${supportCount}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`opposing: ${opposeCount}`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={`${score} points`} />
      </ListItem>
      <ListItem>
        <ListItemText primary={claimBodyFont} />
      </ListItem>
    </List>
  )
}

const RoutePrefixSwitch = ({ibelieve, idontbelieve, somesay, fallback}) => (
  <Switch>
    {ibelieve &&  <Route path="/ibelieve/:slug"><Fragment>{ibelieve}</Fragment></Route>}
    {idontbelieve && <Route path="/idontbelieve/:slug"><Fragment>{idontbelieve}</Fragment></Route>}
    {somesay && <Route path="/somesay/:slug"><Fragment>{somesay}</Fragment></Route>}
    {fallback && <Route path="/"><Fragment>{fallback}</Fragment></Route>}
  </Switch>
)

const CBLink = withStyles({
  link: {
    fontFamily: claimBodyFont
  }
})((props) => <Link className={props.classes.link} {...props}/>)


export const ClaimBodyLink = ({claim: {slug, body}}) => (
  <RoutePrefixSwitch
    ibelieve={<CBLink to={`/ibelieve/${slug}`}>{body}</CBLink>}
    idontbelieve={<Link to={`/idontbelieve/${slug}`}>{body}</Link>}
    somesay={<Link to={`/somesay/${slug}`}>{body}</Link>}
    fallback={<Link to={`/somesay/${slug}`}>{body}</Link>}
  />
)

const withVote = (Component) => graphql(
  queries.VoteOnClaim, {
    props: ({ ownProps: {claim}, mutate }) => ({
      vote: (agreement) => mutate({
        variables: {
          claimID: claim.id,
          agreement
        }
      })
    })
  }
)(Component)

const agreementButton = (voteValue, text) => withVote(
  ({vote, claim: {myAgreement}, onSuccess, ...props}) => (
    <Button color={(myAgreement === voteValue) ? 'primary' : 'default'}
            fontWeight={400}
            onClick={() => vote(voteValue).then(
              ({data: {voteOnClaim: claim}}) => onSuccess && onSuccess(claim)
            )}
            {...props}>
      {text}
    </Button>
  )
)

export const AgreeButton = agreementButton(100, "I agree")
export const DisagreeButton = agreementButton(-100, "I disagree")
export const NotSureButton = agreementButton(0, "I'm not sure")

const SentimentPicker = withRouter(({ history, match: {params: {slug}}}) => (
  <MenuButton menuItems={[
    <MenuItem key="ibelieve"
              onClick={() => goto.iBelieve(history, {slug})}>
      I believe
    </MenuItem>,
    <MenuItem key="idontbelieve"
              onClick={() => goto.iDontBelieve(history, {slug})}>
      I don't believe
    </MenuItem>,
    <MenuItem key="somesay"
              onClick={() => goto.someSay(history, {slug})}>
      some people say
    </MenuItem>
  ]
    }>
    <RoutePrefixSwitch
      ibelieve="I believe"
      idontbelieve="I don't believe"
      somesay="some people say"
    />
  </MenuButton>
))

export const Claim = compose(
  withAuth,
  withRouter,
  withStyles(theme => ({
    claimTooltip: {
      backgroundColor: theme.palette.common.white,
      color: 'rgba(0, 0, 0, 0.87)',
      boxShadow: theme.shadows[1],
      fontSize: 11
    },
    iconButton: {
      float: "right", position: "relative", top: "-2em"
    }
  }))
)(({currentUser, claim, history, classes}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const [commentsShown, setShowComments] = useState(false)
  const {body, slug, creator, myAgreement} = claim

  return (
    <ClaimPaper>
      <Typography variant="h5" align="center">
        <RoutePrefixSwitch
          ibelieve={<SentimentPicker>I believe</SentimentPicker>}
          idontbelieve={<SentimentPicker>I don't believe</SentimentPicker>}
          somesay={<SentimentPicker>some people say</SentimentPicker>}
        />
      </Typography>
      <Tooltip classes={{tooltip: classes.claimTooltip}} interactive
        title={(
          <Fragment>
            <Typography variant="caption" color="textSecondary" align="center">
              created by {creator.username}
            </Typography>
            <ClaimScore claim={claim}/>
            <p>Created at {new Date(claim.createdAt).toString()}</p>
          </Fragment>
        )}>
        <ClaimBody>
          <ClaimBodyLink claim={claim}/>
        </ClaimBody>
      </Tooltip>
      <Typography variant="caption" align="center">{claim.score}</Typography>
      <Typography align="center">
        {(myAgreement !== 100) && (<AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim)}/>)}
        {(myAgreement !== 0) && (<NotSureButton claim={claim} onSuccess={(claim) => goto.someSay(history, claim)}/>)}
        {(myAgreement !== -100) && (<DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim)}/>)}
      </Typography>
      {currentUser && (
        <Typography variant="caption" align="center">
          <RoutePrefixSwitch
            ibelieve={<Link to={believesURL(currentUser.username, slug)}>tell the world!</Link>}
            idontbelieve={<Link to={doesntbelieveURL(currentUser.username, slug)}>tell the world!</Link>}
            fallback={<Link to={isntsureifURL(currentUser.username, slug)}>my view</Link>}
          />

        </Typography>
      )}
      <Typography align="center">
        {!evidenceShown && (
          <Button onClick={() => setShowEvidence(!evidenceShown)}>
            {evidenceShown ? "oh I see" : "but why?"}
          </Button>
        )}
      </Typography>
      <IconButton className={classes.iconButton} onClick={() => setShowComments(!commentsShown)}>
        <Chat/>
      </IconButton>
      <Drawer open={commentsShown} anchor="right" onClose={() => setShowComments(false)}>
        <IconButton onClick={() => setShowComments(false)}><Close/></IconButton>
        <h3>Comments on {body}</h3>
        <Comments claim={claim}/>
      </Drawer>
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
)(({classes, relevanceVote, claim: parentClaim, evidence: {id, supports, claim, myAgreement, relevance, myRelevanceRating}}) => {
  const [expanded, setExpanded] = useState(false)
  return (
    <ExpansionPanel onChange={(e, expanded) => setExpanded(expanded)} elevation={0} className={classes.panel}>
      <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />} className={classes.summary}>
        <StopPropagation width={36} position="absolute" left={-16}>
          <PopoverButton
            px={0} mr={1} minWidth={36} fontWeight={200} fontSize={12}
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
        <Box>
          <Typography variant="h6" fontFamily="claimBody">
            <ClaimBodyLink claim={claim}/>
          </Typography>
          <StopPropagation>
            {(myAgreement !== 100) && (<AgreeButton claim={claim}/>)}
            {(myAgreement !== 0) && (<NotSureButton claim={claim}/>)}
            {(myAgreement !== -100) && (<DisagreeButton claim={claim}/>)}
          </StopPropagation>
        </Box>
      </ExpansionPanelSummary>
      <ExpansionPanelDetails>
          {expanded && (
            <EvidenceLists claim={claim} className={classes.evidenceLists} nested={true} />
          )}
      </ExpansionPanelDetails>
    </ExpansionPanel>
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
          data.evidenceForClaim.push(evidence)
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
    {list && list.filter((evidence) => (evidence.supports === support)).map((evidence) => (
      <Evidence claim={claim} evidence={evidence} key={evidence.id}/>
    ))}
  </Fragment>)

export const EvidenceList = ({claim, evidence, support, placeholder, sentimentMap, nested}) => {
  return (
    <Box mt={2}>
      <Box display="flex">
        <Typography variant={nested? "h6" : "h5"} fontFamily="claimBody">
          <RoutePrefixSwitch {...sentimentMap} />
        </Typography>
        <Box ml={2} flexGrow={2}>
          <EvidenceAdder supports={support} claim={claim} placeholder={placeholder} />
        </Box>
      </Box>
      <Evidences claim={claim} list={evidence} support={support}/>
    </Box>
  )
}

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

const ClaimsGrid = ({claims}) => (
  <Grid container spacing={1}>
    {claims && claims.map((claim) => (
      <Grid item xs={12} key={claim.id}>
        <Claim claim={claim}/>
      </Grid>
    ))}
  </Grid>
)

export default graphql(
  queries.Claims, {
    props: ({data: {claims}}) => ({claims})
  }
)(ClaimsGrid)
