import React, {useEffect} from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Paper} from '../components/ui';
import UserClaim from '../components/UserClaim'
import * as urls from '../urls'


import * as queries from '../queries';

export default compose(
  withRouter,
  graphql(
    queries.UserClaim, {
      props: ({data: {userClaim}}) => ({claim: userClaim}),
      options: ({match: {params: {username, slug}}}) => ({
        variables: {username, slug}
      })

    }
  )
)(({history, match: {params: {username}}, claim}) => {
  useEffect(() => {
    if (claim) {
      history.replace(urls.userView({username}, claim))
    }
  }, [claim])
  return (
    <Paper>
      {claim && <UserClaim username={username} claim={claim} />}
    </Paper>
  )
})
