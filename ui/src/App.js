import React from 'react';
import logo from './logo.svg';
import './App.css';

import { ApolloProvider } from "react-apollo";

import {client} from './clients'
import Claims from './Claims'
import {AuthenticationProvider, withAuth} from './authentication'

const Header = withAuth(({currentUser}) => (
  <header className="App-header">
    <h4>
      TRUTH
    </h4>
    {currentUser && (
      <p>
        welcome, {currentUser.username}
      </p>
    )}
  </header>
))

const App = () => (
  <ApolloProvider client={client}>
    <AuthenticationProvider>
      <div className="App">
        <Header/>
        <Claims/>
      </div>
    </AuthenticationProvider>
  </ApolloProvider>
);


export default App;
