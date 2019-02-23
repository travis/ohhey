import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { withStyles } from '@material-ui/core/styles';
import { Route, Switch, withRouter } from "react-router-dom";

import {
  Paper, Typography, Button, Drawer, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  PopoverButton, List, ListItem, ListItemText, Link, IconButton, Divider, Tooltip, MenuButton, MenuItem
} from './ui'
import { Chat, Close, Create, Add, Remove, ExpandMoreIcon } from './icons'
import {Form, TextInput} from './form'
import Comments from './Comments'
import QuickClaimSearch from './QuickClaimSearch'
import {StopPropagation} from './util'
import {believesURL, doesntbelieveURL, isntsureifURL} from './UserClaim'
import {withAuth} from '../authentication'

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


export const ClaimBodyLink = ({claim: {slug, body}}) => (
  <RoutePrefixSwitch
    ibelieve={<Link to={`/ibelieve/${slug}`}>{body}</Link>}
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
    <Button color={(myAgreement === voteValue) ? 'secondary' : 'default'}
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
  const {body, slug, creator} = claim

  return (
    <Paper>
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
          </Fragment>
        )}>
        <Typography variant="h4" color="textPrimary" align="center">
          <ClaimBodyLink claim={claim}/>
        </Typography>
      </Tooltip>
      <Typography variant="caption" align="center">{claim.score}</Typography>
      <Typography align="center">
        <AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim)}/>
        <NotSureButton claim={claim} onSuccess={(claim) => goto.someSay(history, claim)}/>
        <DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim)}/>
      </Typography>
      {currentUser && (
        <Typography variant="caption" align="center">
          <RoutePrefixSwitch
            ibelieve={<Link to={believesURL(currentUser.username, slug)}>tell the world!</Link>}
            idontbelieve={<Link to={doesntbelieveURL(currentUser.username, slug)}>tell the world!</Link>}
        fallback={<Link to={isntsureifURL(currentUser.username, slug)}>tell the world!</Link>}
          />

        </Typography>
      )}
      <Typography align="center">
        {!evidenceShown && (
          <Button color="primary" onClick={() => setShowEvidence(!evidenceShown)}>
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
    </Paper>
  )
})

export const RelevanceButton = ({relevance, myRelevanceRating, relevanceVote}) =>
      <Button color={(myRelevanceRating === relevance) ? 'secondary' : 'default'}
              onClick={() => relevanceVote(relevance)}>
        {relevance}%
      </Button>


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
)(({classes, relevanceVote, evidence: {id, supports, claim, relevance, myRelevanceRating}}) => {
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
            <EvidenceLists claim={claim} className={classes.evidenceLists}/>
          )}
        </ExpansionPanelDetails>
      </ExpansionPanel>
    </div>
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
  }),
  withStyles(theme => ({
    bodyInput: {
    }
  }))
)(({claim, supports, addEvidence, placeholder, classes}) => {
  return (
    <Form onSubmit={addEvidence}>
      <TextInput field="body" placeholder={placeholder}
                 fullWidth={true} className={classes.bodyInput} autoComplete="off"/>
      <QuickClaimSearch
        claimActions={evidenceClaim => (
          <Button onClick={() => addEvidence({id: evidenceClaim.id})}>add as evidence</Button>
        )}
        or={
          <Fragment>
            <Divider />
            <Button type="submit">Tell the World!</Button>
          </Fragment>
        }/>
    </Form>
  )
})

const Evidences = ({list, support}) => (
  <Fragment>
    {list && list.filter((evidence) => (evidence.supports === support)).map((evidence) => (
      <Evidence evidence={evidence} key={evidence.id}/>
    ))}
  </Fragment>)

const EvidenceList = ({claim, evidence, support, placeholder, sentimentMap}) => {
  const [showAdder, setShowAdder] = useState(false)
  return (
    <Fragment>
      <Typography variant="h5">
        <RoutePrefixSwitch {...sentimentMap} />
        <IconButton onClick={() => setShowAdder(!showAdder)}>
          {showAdder ? <Remove/> : <Add/> }
        </IconButton>
      </Typography>
      {showAdder && (
        <EvidenceAdder supports={support} claim={claim} placeholder={placeholder}/>
      )}
      <Evidences list={evidence} support={support}/>
    </Fragment>
  )
}

const SupportList = ({claim, evidence}) => (
  <EvidenceList claim={claim} support={true} placeholder="why?"
                evidence={evidence}
                sentimentMap={{
                  ibelieve: "because",
                  idontbelieve: "but other people say",
                  somesay: "because",
                  fallback :"because"
                }}/>

)

const OpposeList = ({claim, evidence}) => (
  <EvidenceList claim={claim} support={false} placeholder="why not?"
                evidence={evidence}
                sentimentMap={{
                  ibelieve: "but other people say",
                  idontbelieve: "because",
                  somesay: "but others say",
                  fallback :"but other people say"
                }}/>
)

const EvidenceLists = graphql(
  queries.EvidenceForClaim, {
    options: ({claim}) => ({
      variables: {claimID: claim.id}
    }),
    props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
  }
)(({claim, evidenceList, ...props}) => {
  const Support = () => <SupportList claim={claim} evidence={evidenceList}/>
  const Oppose = () => <OpposeList claim={claim} evidence={evidenceList}/>
  return (
    <div {...props}>
      <RoutePrefixSwitch
        idontbelieve={<Fragment><Oppose/><Support/></Fragment>}
        fallback={<Fragment><Support/><Oppose/></Fragment>}
      />
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
