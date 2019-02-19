import React, { Fragment, useState, useEffect, createRef } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Divider from '@material-ui/core/Divider';

import {Form, TextInput, TextArea} from '../form'
import QuickClaimSearch from '../components/QuickClaimSearch'
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
  const input = createRef()
  useEffect(() => {
    input.current && input.current.focus()
  })
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
      <Form onSubmit={createAndGoToClaim} style={{textAlign: "center"}}>
        <Fragment>
          <TextArea field="body" fullWidth={true} inputRef={input}
                    rows={3}
                    style={{ fontSize: "50px", padding: "0em 0.5em" }}/>
          <QuickClaimSearch or={
            <Fragment>
              <Divider />
              <Button type="submit">Tell the World!</Button>
            </Fragment>
          }/>
        </Fragment>
      </Form>
    </Paper>
  )
})
