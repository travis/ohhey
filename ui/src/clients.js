import { ApolloClient } from 'apollo-client';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { HttpLink } from 'apollo-link-http';
import { onError } from 'apollo-link-error';
import { ApolloLink } from 'apollo-link';
import { RetryLink } from "apollo-link-retry";


//import {createRtdbLink} from 'apollo-link-firebase';
//import * as firebase from 'firebase';

export const makeLinkFrom = (networkLink) => ApolloLink.from([
  new RetryLink(),
  onError(({ graphQLErrors, networkError }) => {
    if (graphQLErrors)
      graphQLErrors.map(({ message, locations, path }) =>
                        console.log(
                          `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`,
                        ),
                       );
    if (networkError) console.log(`[Network error]: ${networkError}`);
  }),
  networkLink
])

export const client = new ApolloClient({
  link: makeLinkFrom(new HttpLink({
    uri: '/graphql',
    credentials: 'same-origin'
  })),
  cache: new InMemoryCache(),
  defaultOptions: {
    query: {
      errorPolicy: 'all'
    },
    watchQuery: {
      errorPolicy: 'all'
    },
    mutate: {
      errorPolicy: 'all'
    }
  }
});
/*

comment out until we can resolve build issues with grpc

// initialize firebase
firebase.initializeApp({
  apiKey: "AIzaSyCHKj3uvM6WvgfPh8Q8-nGcQ-Cq8Zu45_M",
  authDomain: "truth-8a086.firebaseapp.com",
  databaseURL: "https://truth-8a086.firebaseio.com",
  projectId: "truth-8a086",
  storageBucket: "truth-8a086.appspot.com",
  messagingSenderId: "230707137640"
});

// create Realtime Database link
const rtdbLink = createRtdbLink({
  database: firebase.database()
});
*/
export const firebaseClient = client; /*new ApolloClient({
  link: rtdbLink,
  cache: new InMemoryCache()
});*/
