import React from 'react';

import { storiesOf, addDecorator } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { linkTo } from '@storybook/addon-links';
import StoryRouter from 'storybook-react-router';
import { ApolloProvider } from '@apollo/react-components';
import { SnackbarProvider } from 'notistack';

import { client } from './apollo'
import { Spinner, ClaimPaper, ClaimBody } from '../components/ui'
import Claims, { SupportList, Evidence } from '../components/Claims'
import UserClaim from '../components/UserClaim'
import Header from '../components/Header'
import Sources from '../components/Sources'
import { ThemeProvider, fonts } from '../theme'
import { travis, claim, longClaim } from './data'
import { Provider as AuthProvider } from '../authentication'

addDecorator(storyFn => (
  <ThemeProvider>
    <SnackbarProvider>
      <ApolloProvider client={client}>
        <AuthProvider value={{currentUser: travis}}>
          {storyFn()}
        </AuthProvider>
      </ApolloProvider>
    </SnackbarProvider>
  </ThemeProvider>
))

addDecorator(StoryRouter())

storiesOf('UI components', module)
  .add('Spinner', () => <Spinner/>)
  .add('ClaimPaper', () => <ClaimPaper>This is some paper on which to write a claim.</ClaimPaper>)

storiesOf('Header', module)
  .add('default', () =>
       <Header/>
      )

storiesOf('ClaimPaper', module)
  .add('with ClaimBody', () =>
       <ClaimPaper>
         <ClaimBody>This is a claim body</ClaimBody>
       </ClaimPaper>
      )

storiesOf('Claims', module)
  .add('default', () => <Claims />)
  .add("SupportList", () => <SupportList claim={claim}/>)
  .add("Evidence", () => <Evidence claim={claim} evidence={{claim: longClaim, relevance: 66}}/>)

storiesOf('UserClaim', module)
  .add('default', () => <UserClaim username={"toby"} claim={claim} />)

storiesOf('Sources', module)
  .add('default', () => <Sources sources={claim.sources} />)
