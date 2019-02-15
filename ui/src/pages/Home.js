import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Add from '@material-ui/icons/Add';

import {Form, TextArea} from '../form'
import * as queries from '../queries';

const messageForErrorType = (errorType) => {
  if (errorType === "truth.error/unique-conflict") {
    return "Sorry, a claim with that slug already exists!"
  }
}

const messageForError = ({message, extensions}) => {
  if (extensions && extensions.data) {
    const errorType = extensions.data["truth.error/type"]
    if (errorType) {
      return messageForErrorType(errorType)
    } else {
      return message
    }
  } else {
    return message
  }
}


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
)(({createClaim, history, error}) => {
  const [errors, setErrors] = useState([])
  const createAndGoToClaim = (claimInputs) =>
        createClaim(claimInputs)
        .then(({data: {addClaim: claim}, errors}) => {
          if (claim) {
            history.push(`/ibelieve/${claim.slug}`)
          } else {
            setErrors(errors)
          }
        })
  return (
    <Paper>
      <Typography align="center" variant="h2">what do you believe?</Typography>
      {errors.map((error, i) => (
        <div key={i}>{messageForError(error)}</div>
      ))}
      <Form onSubmit={createAndGoToClaim}>
        <TextArea field="body"/>
        <IconButton type="submit"><Add/>Create a Claim</IconButton>
      </Form>
    </Paper>
  )
})
