import React, { Fragment, useState, useEffect, createRef } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";

import {Form, TextInput} from '../components/form'
import {Paper, Typography, Button, Divider} from '../components/ui'
import QuickClaimSearch from '../components/QuickClaimSearch'
import {AgreeButton, DisagreeButton} from '../components/Claims'
import * as queries from '../queries';
import * as goto from '../goto';

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
            goto.iBelieve(history, claim)
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
          <TextInput field="body" fullWidth={true} inputRef={input}
                     autoComplete="off"
                     style={{ fontSize: "50px", padding: "0em 0.5em" }}/>
          <QuickClaimSearch
            claimActions={claim => (
              <Fragment>
                <AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim)}/>
                <DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim)}/>
              </Fragment>
            )}
            or={
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
