import React, {Fragment} from 'react';
//import logo from './logo.svg';
//import './App.css';

import { ApolloProvider } from "react-apollo";
import { BrowserRouter, Route } from "react-router-dom";

import {client} from './clients'
import Claims from './Claims'
import ClaimPage from './pages/Claim'
import HomePage from './pages/Home'
import {AuthenticationProvider, withAuth} from './authentication'
import CssBaseline from '@material-ui/core/CssBaseline';


const Header = withAuth(({currentUser}) => (
  <header className="App-header">
    TRUTH
    {currentUser && (

      <Fragment>  welcome, {currentUser.username} </Fragment>
    )}
  </header>
))


const App = () => (
  <Fragment>
    <CssBaseline/>
    <ApolloProvider client={client}>
      <AuthenticationProvider>
        <BrowserRouter>
          <div className="App">
            <Route exact path="/" component={HomePage}/>
            <Route path="/claims" component={Claims}/>
            <Route path="/ibelieve/:slug" component={ClaimPage}/>
          </div>
        </BrowserRouter>
      </AuthenticationProvider>
    </ApolloProvider>
  </Fragment>
);


export default App;
