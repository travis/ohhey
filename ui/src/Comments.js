import React, { Fragment, useState } from 'react';
import {  graphql, compose } from "react-apollo";

import {firebaseClient} from './clients'
import * as queries from './queries';

import {Form, Text} from './form'

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
          data.comments.push(newComment)
          client.writeQuery({...cacheSpec, data})
        } catch(err){
          console.log("err in sub handler!", err)
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
    <div>
      {comments && comments.map(({id, body}, i) => (
        <div key={i}>{body}</div>
      ))}
      <Form onSubmit={({comment}) => createComment(comment)}>
        <Text field="comment"/>
        <button type="submit">Comment</button>
      </Form>
      <button onClick={() => createComment("HI!")}>Say Hi</button>
      <button onClick={() => createComment("hello")}>Say hello</button>
    </div>
  )
)
