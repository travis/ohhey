import React, {Fragment} from 'react';
//import logo from './logo.svg';
//import './App.css';

import { ApolloProvider } from "react-apollo";
import { BrowserRouter, Route, Link } from "react-router-dom";

import {client} from './clients'
import Claims from './Claims'
import ClaimPage from './pages/Claim'
import Home from './Home'
import {AuthenticationProvider, withAuth} from './authentication'

const Header = withAuth(({currentUser}) => (
  <header className="App-header">
    TRUTH
    {currentUser && (

      <Fragment>  welcome, {currentUser.username} </Fragment>
    )}
  </header>
))


const App = () => (
  <ApolloProvider client={client}>
    <AuthenticationProvider>
      <BrowserRouter>
        <div className="App">
          <Route exact path="/" component={Home}/>
          <Route path="/claims" component={Claims}/>
          <Route path="/ibelieve/:slug" component={ClaimPage}/>
        </div>
      </BrowserRouter>
    </AuthenticationProvider>
  </ApolloProvider>
);


export default App;
