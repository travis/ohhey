import React from 'react';
import { compose } from "../util";

import { withAuth } from '../authentication'
import {Form, TextInput} from '../components/form'
import { Button} from '../components/ui';

export default compose(
  withAuth
)(({authData: {currentUser, logIn, logOut}}) => (
  currentUser ? (
    <div>
      <h3>You are logged in as {currentUser.username}</h3>
      <Button onClick={logOut}>log out</Button>
    </div>
  ) : (
    <Form onSubmit={logIn}>
      <TextInput field="username"/>
      <TextInput field="password" type="password"/>
      <Button type="submit">log in</Button>
    </Form>
  )
))
