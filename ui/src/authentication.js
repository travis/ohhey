import React, {createContext} from 'react';
import { graphql, withApollo } from '@apollo/react-hoc'
import { compose } from './util'
import * as queries from './queries'

export const {Consumer, Provider} = createContext({
  currentUser: null
});

export const AuthenticationProvider = compose(
  withApollo,
  graphql(queries.CurrentUser, {
    props: ({ownProps, data: {loading, currentUser, refetch}}) => ({
      currentUser,
      userLoading: loading,
      refetchUser: refetch
    })
  }),
  graphql(queries.LogIn, {
    props: ({ownProps: {client}, mutate }) => ({
      logIn: (variables) => mutate({
        variables,
        update: (cache, {data: {logIn}}) => {
          client.resetStore()
        }
      })
    })
  }),
  graphql(queries.LogOut, {
    props: ({ownProps: {client}, mutate }) => ({
      logOut: () => mutate({
        update: (cache) => {
          client.resetStore()
        }
      })
    })
  })
)(({currentUser, userLoading, refetchUser, logIn, logOut, children}) => (
  <Provider value={{ currentUser, userLoading, refetchUser, logIn, logOut }}>
    {children}
  </Provider>
))

export const withAuth = (Authable) => (props) => (
  <Consumer>
    { (authData) => (<Authable authData={authData} {...props}/>) }
  </Consumer>
)
