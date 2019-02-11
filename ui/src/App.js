import React, { Component, Fragment } from 'react';
import logo from './logo.svg';
import './App.css';

import ApolloClient from "apollo-boost";
import { ApolloProvider, graphql } from "react-apollo";
import * as queries from './queries';

const client = new ApolloClient({
  uri: "/graphql"
});

const Claim = ({claim: {body, agreeCount, disagreeCount, supportCount, opposeCount}}) => (
  <div>
    <h5>{body}</h5>
    a: {agreeCount} d: {disagreeCount} s: {supportCount} o: {opposeCount}
  </div>
)

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
            <img src={logo} className="App-logo" alt="logo" />
            <p>
              Edit <code>src/App.js</code> and save to reload.
            </p>
            <a
              className="App-link"
              href="https://reactjs.org"
              target="_blank"
              rel="noopener noreferrer"
            >
              Learn React
            </a>
          </header>

          <Claims/>
        </div>
      </ApolloProvider>
    );
  }
}

export default App;
