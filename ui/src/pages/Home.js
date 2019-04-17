import React, { Fragment, useState, useEffect, createRef } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";
import { withStyles } from '@material-ui/core/styles';

import {Spinner} from '../components/ui'
import {Form } from '../components/form'
import {Typography, Button, Divider, ClaimPaper} from '../components/ui'
import AutosuggestClaimTextInput from '../components/AutosuggestClaimTextInput'
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
    },
    spinner: {
      margin: "auto"
    }
  }))
)(({classes, createClaim, history, error}) => {
  const [errors, setErrors] = useState([])
  const input = createRef()
  useEffect(() => {
    input.current && input.current.focus()
  })
  const [submitting, setSubmitting] = useState(false)
  const createAndGoToClaim = async (claimInputs) => {
    setSubmitting(true)
    const {data: {addClaim: claim}, errors} = await createClaim(claimInputs)
    setSubmitting(false)
    if (claim) {
      goto.iBelieve(history, claim, 'push')
    } else {
      setErrors(errors)
    }
  }

  return (
    <ClaimPaper>
      <Typography align="center" variant="h2" color="textPrimary" fontFamily="claimBody">what do you believe?</Typography>
      {errors.map((error, i) => (
        <div key={i}>{messageForError(error)}</div>
      ))}
      <Form className={classes.form} onSubmit={createAndGoToClaim}>
        {({formState: {errors: {body: bodyError}}}) => (
          <Fragment>
            {bodyError && <Typography color="error">{bodyError}</Typography>}
            <AutosuggestClaimTextInput
              field="body"
              error={bodyError}
              fullWidth={true}
              inputRef={input}
              validate={validations.claimBody}
              validateOnChange
              autoComplete="off"
              className={classes.bodyInput}

              onSuggestionSelected={(event, {suggestion: {result: claim}}) => {
                event.preventDefault();
                goto.claim(history, claim, 'push')
              }}

              claimActions={claim => (
                <Fragment>
                  <AgreeButton claim={claim} onSuccess={(claim) => goto.iBelieve(history, claim, 'push')}/>
                  <DisagreeButton claim={claim} onSuccess={(claim) => goto.iDontBelieve(history, claim, 'push')}/>
                </Fragment>
              )}
              create={query =>
                <Fragment>
                  <Divider />
                  {submitting ?
                   <Spinner className={classes.spinner}/> :
                   <Button type="submit">Tell the World!</Button>
                  }
                </Fragment>
              }
            />
            {/*<QuickClaimSearch
            create={
              <Fragment>
                <Divider />
                {submitting ?
                   <Spinner className={classes.spinner}/> :
                   <Button type="submit">Tell the World!</Button>
                }
              </Fragment>
              }/>*/}
          </Fragment>
        )}
      </Form>
    </ClaimPaper>
  )
})
