import React, { Fragment, useState, useEffect, createRef } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";
import { withStyles } from '@material-ui/core/styles';

import {Form, TextInput} from '../components/form'
import {Paper, Typography, Button, Divider} from '../components/ui'
import QuickClaimSearch from '../components/QuickClaimSearch'
import {AgreeButton, DisagreeButton} from '../components/Claims'
import * as queries from '../queries';
import * as goto from '../goto';
import * as validations from '../validations';

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
  ),
  withStyles(theme => ({
    form: {
      textAlign: "center"
    },
    bodyInput: {
      fontSize: "50px", padding: "0em 0.5em"
    }
  }))
)(({classes, createClaim, history, error}) => {
  const [errors, setErrors] = useState([])
  const input = createRef()
  useEffect(() => {
    input.current && input.current.focus()
  })
  const createAndGoToClaim = (claimInputs) =>
        createClaim(claimInputs)
        .then(({data: {addClaim: claim}, errors}) => {
          if (claim) {
            goto.iBelieve(history, claim, 'push')
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
      <Form className={classes.form} onSubmit={createAndGoToClaim}>
        {({formState: {errors: {body: bodyError}}}) => (
          <Fragment>
            {bodyError && <Typography color="error">{bodyError}</Typography>}
            <TextInput field="body"
                       error={bodyError}
                       fullWidth={true}
                       forwardedRef={input}
                       validate={validations.claimBody}
                       validateOnChange
                       autoComplete="off"
                       className={classes.bodyInput}
          />
          <QuickClaimSearch
            claimActions={claim => (
              <Fragment>
                <AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim, 'push')}/>
                <DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim, 'push')}/>
              </Fragment>
            )}
            create={
              <Fragment>
                <Divider />
                <Button type="submit">Tell the World!</Button>
              </Fragment>
            }/>
          </Fragment>
        )}
      </Form>
    </Paper>
  )
})
