import React, {Fragment, useEffect} from 'react';
import { graphql } from "@apollo/react-hoc";
import { compose } from "../util";
import { withRouter } from "react-router-dom";

import {Claim} from '../components/Claims'
import { ClaimSpinner } from '../components/ui'

import * as goto from '../goto';
import * as queries from '../queries';

export default compose(
  withRouter,
  graphql(
    queries.Claim, {
      props: ({data: {claim, loading, error}}) => ({claim, claimLoading: loading, claimError: error}),
      options: ({match: {params: {slug}}}) => ({
        variables: {slug}
      })

    }
  )
)(({history, claim, claimLoading}) => {
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
      {claimLoading ? (
        <ClaimSpinner />
      ) : (
        <Claim claim={claim} />
      )}
    </Fragment>
  )
})
