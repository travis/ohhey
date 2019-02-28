import React, {createContext} from 'react';
import { graphql, compose, withApollo } from 'react-apollo'
import * as queries from './queries'

const {Consumer, Provider} = createContext({
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
    { (authProps) => (<Authable {...authProps} {...props}/>) }
  </Consumer>
)
