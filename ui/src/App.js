import React from 'react';
import logo from './logo.svg';
import './App.css';

import { ApolloProvider } from "react-apollo";

import {client} from './clients'
import Claims from './Claims'


const App = () => (
  <ApolloProvider client={client}>
    <div className="App">
      <header className="App-header">
        <p>
          TRUTH
        </p>
      </header>

      <Claims/>
    </div>
  </ApolloProvider>
);


export default App;
