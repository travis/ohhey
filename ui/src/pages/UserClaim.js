import React, {useEffect} from 'react';
import { graphql } from "@apollo/react-hoc";
import { compose } from "../util";
import { withRouter } from "react-router-dom";

import {ClaimSpinner, Paper} from '../components/ui';
import UserClaim from '../components/UserClaim'
import * as urls from '../urls'


import * as queries from '../queries';

export default compose(
  withRouter,
  graphql(
    queries.UserClaim, {
      props: ({data: {claim, loading, error}}) => (
        {claim, claimLoading: loading, claimError: error}
      ),
      options: ({match: {params: {username, slug}}}) => ({
        variables: {username, slug}
      })

    }
  )
)(({history, match: {params: {username}}, claim, claimLoading}) => {
  useEffect(() => {
    if (claim) {
      history.replace(urls.userView(claim))
    }
  }, [claim, history])
  return claimLoading ? (
    <ClaimSpinner/>
  ) : (
    <Paper>
      <UserClaim username={username} claim={claim} />
    </Paper>
  )
})
