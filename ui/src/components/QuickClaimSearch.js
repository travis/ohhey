import React, {Fragment} from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";
import {withFormState} from 'informed'
import Paper from '@material-ui/core/Paper';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

import {Form, TextInput} from '../form'
import Link from '../Link'
import {ClaimBodyLink, AgreeButton, DisagreeButton, NotSureButton} from '../Claims'

import * as queries from '../queries';

const claimStyles = theme => ({
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular
  }
})

const getSearchTerm = ({values: {body}}) => body

const StopPropagation = ({children}) => (
  <span onClick={(e) => e.stopPropagation()}>
    {children}
  </span>
)

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
        console.log("FS", formState)
        console.log("FT", getSearchTerm(formState))
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
       <Claim claim={claim}/>
     ))) :
     or
    }
  </Fragment>
))
