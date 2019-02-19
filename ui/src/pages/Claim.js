import React from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Paper} from '../components/ui';
import {Claim} from '../components/Claims'

import * as queries from '../queries';

export default compose(
  withRouter,
  graphql(
    queries.Claim, {
      props: ({data: {claim}}) => ({claim}),
      options: ({match: {params: {slug}}}) => ({
        variables: {slug}
      })

    }
  )
)(({claim}) => (
  <Paper>
    {claim && <Claim claim={claim} />}
  </Paper>
))
