import React, {useEffect} from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Paper} from '../components/ui';
import UserClaim, {believesURL, doesntbelieveURL, isntsureifURL} from '../components/UserClaim'

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
      const {slug, agreement } = claim
      console.log("navigating!")
      if (agreement === 100) {
        history.replace(believesURL(username, slug))
      } else if (agreement === -100) {
        history.replace(doesntbelieveURL(username, slug))
      } else {
        history.replace(isntsureifURL(username, slug))
      }
    }
  }, [claim])
  return (
    <Paper>
      {claim && <UserClaim username={username} claim={claim} />}
    </Paper>
  )
})
