import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import { Button, Box, Layer, Heading } from 'grommet'
import {
  Add, Like, Dislike, Chat, ChatOption, AddCircle, SubtractCircle, New, Close,
  WifiNone, Wifi, WifiLow, WifiMedium
} from "grommet-icons";

import {Form, TextArea} from './form'
import * as queries from './queries';

export default compose(
  withRouter,
  graphql(
    queries.AddClaim, {
      props: ({ mutate }) => ({
        createClaim: (claim) => mutate({
          variables: {
            claim
          }
        })
      })
    }
  )
)(({createClaim, history}) => {
  const createAndGoToClaim = (claimInputs) => createClaim(claimInputs).
        then(({data: {addClaim: {slug}}}) => history.push(`/ibelieve/${slug}`))
  return (
    <Box>
      <Heading textAlign="center">what do you believe?</Heading>
      <Form onSubmit={createAndGoToClaim}>
        <TextArea field="body"/>
        <Button type="submit" icon={<Add/>} label="Add"/>
      </Form>
    </Box>
  )
})
