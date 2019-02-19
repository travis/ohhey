import React, {Fragment} from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Form, TextInput} from '../components/form'
import {Paper, Button} from '../components/ui';
import {Claim} from '../components/Claims'

import * as queries from '../queries';

const getSearchTerm = (location) =>
      new URLSearchParams(location.search).get("q")

export default compose(
  withRouter,
  graphql(
    queries.SearchClaims, {
      skip: ({location}) => {
        const term = getSearchTerm(location)
        return !term || (term === '')
      },
      props: ({data: {searchClaims: claimsSearch}}) => ({claimsSearch}),
      options: ({location}) => {
        return ({
          variables: {term: getSearchTerm(location)}
        })
      }
    }
  )
)(({claimsSearch, history}) => (
  <Paper>
    <Form onSubmit={({term}) => history.push(`?q=${term}`)}>
      <TextInput field="term"/>
      <Button type="submit">Search</Button>
    </Form>
    {claimsSearch && (
      <Fragment>
        total results: {claimsSearch.totalCount}
        {claimsSearch && claimsSearch.results.map(({score, result: claim}) => (
          <Claim claim={claim}/>
        ))}
      </Fragment>
    )}
  </Paper>
))
