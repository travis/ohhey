import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { Button, Box, Layer } from 'grommet'
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
  <Box>
    {claim && <Claim claim={claim} />}
  </Box>
))
