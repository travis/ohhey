import React, {Fragment, useEffect} from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Claim} from '../components/Claims'

import * as goto from '../goto';
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
)(({history, claim}) => {
  useEffect(() => {
    if (claim) {
      const myAgreement = claim.myAgreement
      if (myAgreement === 100) {
        goto.iBelieve(history, claim, 'replace')
      } else if (myAgreement === -100) {
        goto.iDontBelieve(history, claim, 'replace')
      } else {
        goto.someSay(history, claim, 'replace')
      }
    }
  }, [claim, history])
  return (
    <Fragment>
      {claim && <Claim claim={claim} />}
    </Fragment>
  )
})
