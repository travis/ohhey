import React, {Fragment} from 'react';
import { graphql, compose } from "react-apollo";
import { withFormState } from 'informed'
import { withStyles } from '@material-ui/core/styles';
import {
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails, Typography
} from './ui'
import { ExpandMoreIcon } from './icons'
import { ClaimBodyLink } from './Claims'

import {StopPropagation} from './util'

import * as queries from '../queries';

const claimStyles = theme => ({
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular
  }
})

const getSearchTerm = ({values: {body}}) => body

const Claim = withStyles(claimStyles)(({claim, results, classes, actions}) => (
  <ExpansionPanel>
    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
      <Typography className={classes.heading}>
        <ClaimBodyLink claim={claim}/>
      </Typography>
      <StopPropagation>
        {actions && actions(claim, results)}
      </StopPropagation>
    </ExpansionPanelSummary>
    <ExpansionPanelDetails>
      score:
      <Typography variant="subtitle1">{claim.score}</Typography>
    </ExpansionPanelDetails>
  </ExpansionPanel>
))

const doResultsInclude = (claimsSearch, value) =>
      claimsSearch && claimsSearch.results.map(r => r.result.body).find(body => body === value)

export default compose(
  withFormState,
  graphql(
    queries.QuickSearchClaims, {
      skip: ({formState}) => {
        const term = getSearchTerm(formState)
        return !term || (term === '')
      },
      props: ({data: {suggestClaims: claimsSearch}}) => (
        {claimsSearch}
      ),
      options: ({formState}) => {
        return ({
          variables: {term: getSearchTerm(formState)}
        })
      }
    }
  )
)(({claimsSearch, history, formState, create, claimActions}) => (
  <Fragment>
    {claimsSearch && (claimsSearch.results.length > 0) &&
     (claimsSearch.results.map(({result: claim}) => (
       <Claim claim={claim} results={claimsSearch.results} actions={claimActions} key={claim.id}/>
     )))
    }
    {doResultsInclude(claimsSearch, formState.values.body) ? "" : create}
  </Fragment>
))
