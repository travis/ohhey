import React from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Paper} from '../components/ui';
import UserClaim from '../components/UserClaim'

import * as queries from '../queries';

export default compose(
  withRouter,
  graphql(
    queries.Claim, {
      props: ({data: {claim}}) => ({claim}),
      options: ({match: {params: {username, slug}}}) => ({
        variables: {slug}
      })

    }
  )
)(({match: {params: {username}}, claim}) => (
  <Paper>
    {claim && <UserClaim username={username} claim={claim} />}
  </Paper>
))
