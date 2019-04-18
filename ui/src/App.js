import React, {Fragment} from 'react';

import { ApolloProvider } from "react-apollo";
import { BrowserRouter, Route, Switch } from "react-router-dom";
import { SnackbarProvider } from 'notistack';

import { withStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box'

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
import {ThemeProvider} from './theme'


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

const Content = (props) => <Box m={1} {...props}/>

const UI = withAuth(({authData: {currentUser, userLoading, logIn, logOut}}) => (
  <Fragment>
    <CssBaseline/>
    {!userLoading && <Header/>}
    {currentUser ? (
      <Content>
        <Switch>
          <Route exact path="/" component={HomePage}/>
          <Route path="/all" component={Claims}/>
          <Route path="/search" component={SearchPage}/>
          <Route path="/ibelieve/:slug" component={ClaimPage}/>
          <Route path="/idontbelieve/:slug" component={ClaimPage}/>
          <Route path="/somesay/:slug" component={ClaimPage}/>
          <Route path="/someonenamed/:username/believes/:slug" component={UserClaimPage}/>
          <Route path="/someonenamed/:username/doesntbelieve/:slug" component={UserClaimPage}/>
          <Route path="/someonenamed/:username/isntsureif/:slug" component={UserClaimPage}/>
        </Switch>
      </Content>
    ) : (
      <TeaserVideo/>
    )}
  </Fragment>
))

const App = () => (
  <ThemeProvider>
    <ApolloProvider client={client}>
      <SnackbarProvider>
        <AuthenticationProvider>
          <BrowserRouter>
            <UI/>
          </BrowserRouter>
        </AuthenticationProvider>
      </SnackbarProvider>
    </ApolloProvider>
  </ThemeProvider>
);


export default App;
