import React, {Fragment} from 'react';
import { graphql, compose } from "react-apollo";
import { withFormState } from 'informed'
import { withStyles } from '@material-ui/core/styles';
import {
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails, Typography
} from './ui'
import { ExpandMoreIcon } from './icons'
import {ClaimBodyLink, AgreeButton, DisagreeButton} from './Claims'

import {StopPropagation} from './util'

import * as queries from '../queries';

const claimStyles = theme => ({
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular
  }
})

const getSearchTerm = ({values: {body}}) => body

const Claim = withStyles(claimStyles)(({claim, classes}) => (
  <ExpansionPanel>
    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
      <Typography className={classes.heading}>
        <ClaimBodyLink claim={claim}/>
      </Typography>
      <StopPropagation>
        <AgreeButton claim={claim}/>
      </StopPropagation>
      <StopPropagation>
        <DisagreeButton claim={claim}/>
      </StopPropagation>
    </ExpansionPanelSummary>
    <ExpansionPanelDetails>
      <Typography>
        score:
        <Typography variant="subtitle1">{claim.score}</Typography>
      </Typography>
    </ExpansionPanelDetails>
  </ExpansionPanel>
))

export default compose(
  withFormState,
  graphql(
    queries.QuickSearchClaims, {
      skip: ({formState}) => {
        const term = getSearchTerm(formState)
        return !term || (term === '')
      },
      props: ({data: {searchClaims: claimsSearch}}) => (
        {claimsSearch}
      ),
      options: ({formState}) => {
        return ({
          variables: {term: getSearchTerm(formState)}
        })
      }
    }
  )
)(({claimsSearch, history, formState, or}) => (
  <Fragment>
    {(claimsSearch && (claimsSearch.results.length > 0)) ?
     (claimsSearch.results.map(({result: claim}) => (
       <Claim claim={claim} key={claim.id}/>
     ))) :
     or
    }
  </Fragment>
))
