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
  })
)(({currentUser, userLoading, refetchUser, children}) => (
  <Provider value={{ currentUser, userLoading, refetchUser }}>
    {children}
  </Provider>
))

export const withAuth = (Authable) => (props) => (
  <Consumer>
    { (authProps) => (<Authable {...authProps} {...props}/>) }
  </Consumer>
)
