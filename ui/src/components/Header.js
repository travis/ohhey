import React, {Fragment} from 'react';

import {Form, TextInput} from './form'
import {Button} from '../components/ui'
import { withAuth } from '../authentication'

export default withAuth(({currentUser, logIn, logOut}) => (
  <header className="App-header">
    {currentUser ? (
      <Fragment>
        welcome, {currentUser.username}
        <Button onClick={logOut}>log out</Button>
      </Fragment>
    ) : (
      <Form onSubmit={logIn}>
        <TextInput field="username"/>
        <TextInput field="password" type="password"/>
        <Button type="submit">log in</Button>
      </Form>
    )}
  </header>
))
