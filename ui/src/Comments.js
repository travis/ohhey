import React, { Fragment, useState } from 'react';
import {  graphql, compose } from "react-apollo";

import {firebaseClient} from './clients'
import * as queries from './queries';

import {Form, TextArea} from './form'
import { Button, Box, Layer } from 'grommet'

const commentInList = (comments, comment) =>
      (comments.filter((existingComment) => existingComment.id == comment.id).length > 0)

export default compose(
  graphql(queries.CommentsQuery, {
    options: ({claim}) => ({
      variables: {ref: `/claims/${claim.id}/comments`},
      client: firebaseClient
    }),
    props: ({data: {comments}}) => ({comments})
  }),
  graphql(queries.SubscribeToComments, {
    options: ({claim}) => ({
      variables: {ref: `/claims/${claim.id}/comments`},
      onSubscriptionData: ({client, subscriptionData: {data: {newComment}}}) => {
        try {
          const cacheSpec = {
            query: queries.CommentsQuery,
            variables: { ref: `/claims/${claim.id}/comments` }
          };
          const data = client.readQuery(cacheSpec)
          if (data.comments && !commentInList(data.comments, newComment)) {
            data.comments.push(newComment)
            client.writeQuery({...cacheSpec, data})
          }
        } catch(err){
          //console.log("err in sub handler!", err)
        }

      },
      client: firebaseClient
    })
  }),
  graphql(queries.CreateComment, {
    options: (props) => ({
      client: firebaseClient
    }),
    props: ({ ownProps: {claim}, mutate }) => ({
      createComment: (body) => mutate({
        variables: {
          ref: `/claims/${claim.id}/comments`,
          input: {body}
        }
      })
    })
  })
)(
  ({comments, createComment}) => (
    <Fragment>
      {comments && comments.map(({id, body}) => (
        <span key={id}>
          {body}
        </span>
      ))}
      <Form onSubmit={({comment}) => createComment(comment)}>
        <TextArea field="comment"/>
        <Button type="submit" label="Comment"/>
        <Button label="Say Hi" onClick={() => createComment("HI!")}/>
        <Button label="Say hello" onClick={() => createComment("hello")}/>
      </Form>
    </Fragment>
  )
)
