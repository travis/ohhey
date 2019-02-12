import React, { Component, Fragment, useState } from 'react';
import logo from './logo.svg';
import './App.css';

import ApolloClient from "apollo-boost";
import { ApolloProvider, graphql, compose } from "react-apollo";
import * as queries from './queries';

import {client, firebaseClient} from './clients'

const Evidence = ({evidence: {id, supports, claim, relevance}}) => {
  const color = supports ? 'blue' : 'red'
  return (
    <div key={id} style={{color, border: `1px solid ${color}`}}>
      {relevance} % relevant
      <Claim claim={claim} key={claim.id}/>
    </div>
  )
}

const EvidenceList = graphql(queries.EvidenceForClaim, {
  props: ({data: {evidenceForClaim}}) => ({evidenceList: evidenceForClaim})
})(({claim, evidenceList}) => (
    <Fragment>
      {evidenceList && evidenceList.map((evidence) => (
        <Evidence evidence={evidence}/>
      ))}
    </Fragment>
  )
)

const Comments = compose(
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
    <button onClick={() => createComment("HI!")}>Say Hi</button>
    <button onClick={() => createComment("hello")}>Say hello</button>
  </div>
)
                          )

const Claim = ({claim}) => {
  const [evidenceShown, setShowEvidence] = useState(false)
  const {id, body, agreeCount, disagreeCount, supportCount, opposeCount} = claim
  return (
    <div style={{border: "1px dotted black"}}>
      <h5>{body}</h5>
      a: {agreeCount} d: {disagreeCount} s: {supportCount} o: {opposeCount}
      <Comments claim={claim}/>
      <button onClick={() => setShowEvidence(!evidenceShown)}>{evidenceShown ? "Hide" : "Show"} Evidence</button>
      {evidenceShown ? (
        <EvidenceList claimID={id}/>
      ) : ("")}
    </div>
  )
}

const Claims = graphql(queries.Claims, {
  props: ({data: {claims}}) => ({claims})
}
)(
  ({claims}) => (
  <Fragment>
    {claims && claims.map((claim) => (
      <Claim claim={claim} key={claim.id}/>
    ))}
  </Fragment>
))

class App extends Component {
  render() {
    return (
      <ApolloProvider client={client}>
        <div className="App">
          <header className="App-header">
            <p>
              TRUTH
            </p>
          </header>

          <Claims/>
        </div>
      </ApolloProvider>
    );
  }
}

export default App;
