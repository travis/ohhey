import React, {Fragment} from 'react';
//import logo from './logo.svg';
//import './App.css';

import { ApolloProvider } from "react-apollo";
import { BrowserRouter, Route, Switch } from "react-router-dom";

import { withStyles } from '@material-ui/core/styles';

import {client} from './clients'
import Claims from './components/Claims'
import ClaimPage from './pages/Claim'
import UserClaimPage from './pages/UserClaim'
import HomePage from './pages/Home'
import SearchPage from './pages/Search'
import {AuthenticationProvider } from './authentication'
import CssBaseline from '@material-ui/core/CssBaseline';
import Header from './components/Header'
import { withAuth } from './authentication'

const TeaserVideo = withStyles(theme => ({
  teaserVideo: {
    zIndex: "-1000",
    position: "fixed",
    top: 0,
    minWidth: "100%",
    minHeight: "100%"
  }
}))(({classes}) => (
  <video className={classes.teaserVideo} autoPlay muted loop>
    <source src="hello.mp4" type="video/mp4"/>
  </video>

))

const UI = withAuth(({currentUser, logIn, logOut}) => (
  <Fragment>
    <Header/>
    {currentUser ? (
      <Switch>
        <Route exact path="/" component={HomePage}/>
        <Route path="/claims" component={Claims}/>
        <Route path="/search" component={SearchPage}/>
        <Route path="/ibelieve/:slug" component={ClaimPage}/>
        <Route path="/idontbelieve/:slug" component={ClaimPage}/>
        <Route path="/somesay/:slug" component={ClaimPage}/>
        <Route path="/someonenamed/:username/believes/:slug" component={UserClaimPage}/>
        <Route path="/someonenamed/:username/doesntbelieve/:slug" component={UserClaimPage}/>
        <Route path="/someonenamed/:username/isntsureif/:slug" component={UserClaimPage}/>
      </Switch>
    ) : (
      <TeaserVideo/>
    )}
  </Fragment>
))


const App = () => (
  <Fragment>
    <CssBaseline/>
    <ApolloProvider client={client}>
      <AuthenticationProvider>
        <BrowserRouter>
          <UI/>
        </BrowserRouter>
      </AuthenticationProvider>
    </ApolloProvider>
  </Fragment>
);


export default App;
