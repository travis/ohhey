import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

import ApolloClient from "apollo-boost";
import { ApolloProvider, graphql } from "react-apollo";
import * as queries from './queries';

const client = new ApolloClient({
  uri: "/graphql"
});

const Claims = graphql(queries.Claims, {
  props: ({data: {claims}}) => ({claims})
}
)(
  ({claims}) => (
  <ul>
    {claims && claims.map(({body}) => (
      <li>{body}</li>
    ))}
  </ul>
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
