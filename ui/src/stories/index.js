import React from 'react';

import { storiesOf, addDecorator } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { linkTo } from '@storybook/addon-links';
import StoryRouter from 'storybook-react-router';
import { ApolloProvider } from 'react-apollo';

import { client } from './apollo'
import { Spinner, ClaimPaper, ClaimBody } from '../components/ui'
import Claims from '../components/Claims'
import Header from '../components/Header'
import { ThemeProvider, fonts } from '../theme'
import { ThemeProvider as StyledComponentsThemeProvider } from 'styled-components';

addDecorator(storyFn => (
  <ThemeProvider>
    <ApolloProvider client={client}>
      {storyFn()}
    </ApolloProvider>
  </ThemeProvider>
))

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

const claimsStories = storiesOf('Claims', module)
  .addDecorator(StoryRouter())
  .add('with text', () =>
       <Claims />
      )

fonts.map(font => claimsStories.add(`with ${font}`, () => (
  <StyledComponentsThemeProvider theme={(theme) => ({...theme, claimBodyFont: font})}>
    <Claims />
  </StyledComponentsThemeProvider>
)))
