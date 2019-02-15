import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import Paper from '@material-ui/core/Paper';
import { withRouter } from "react-router-dom";

import {Claim} from '../Claims'

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
