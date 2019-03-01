import React, {Fragment} from 'react';
import { withStyles } from '@material-ui/core/styles';

import {Form, TextInput} from './form'
import {Link, Button, AppBar, Toolbar, MenuButton, MenuItem, Typography} from '../components/ui'
import {AccountCircle} from '../components/icons'
import { withAuth } from '../authentication'
import logo from '../logo.svg';
import {claimBodyFont} from '../theme'

const HeaderLogo = withStyles(theme => (console.log(theme)) || {
  logo: {
    color: theme.palette.primary,
    display: "inline",
    width: "2em"
  }
})(
  ({classes}) => <img className={classes.logo} src={logo}/>
)

export default withStyles({
  ohhey: {
    flexGrow: 1,
    marginLeft: '0.5em',
    fontFamily: claimBodyFont
  }
})(withAuth(({currentUser, logIn, logOut, classes}) => (
  <header>
    <AppBar color="default" position="static">
      <Toolbar>
        <Link to="/"><HeaderLogo/></Link>
        <Typography variant="h6" className={classes.ohhey}>
          oh hey!
        </Typography>
      {currentUser ? (
        <Fragment>
          <MenuButton menuItems={[
            <MenuItem key="welcome">
              welcome, {currentUser.username}
            </MenuItem>,
            <MenuItem key="logout">
              <Button onClick={logOut}>log out</Button>
            </MenuItem>
          ]}>
            <AccountCircle color="primary"/>
          </MenuButton>
        </Fragment>
      ) : (
        <MenuButton menuItems={
          <MenuItem>
            <Form onSubmit={logIn}>
              <TextInput field="username"/>
              <TextInput field="password" type="password"/>
              <Button type="submit">log in</Button>
            </Form>
          </MenuItem>
        }>
          <Typography variant="h6">
            Log In
          </Typography>
        </MenuButton>
      )}
      </Toolbar>
    </AppBar>
  </header>
)))
