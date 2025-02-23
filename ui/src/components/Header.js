import React from 'react';
import { withStyles } from '@material-ui/core/styles';

//import {Form, TextInput} from './form'
import {Link, Button, AppBar, Toolbar, MenuButton, MenuItem, Typography} from '../components/ui'
import {AccountCircle} from '../components/icons'
import { withAuth } from '../authentication'
import logo from '../images/logo.svg';

const HeaderLogo = withStyles(theme => ({
  logo: {
    color: theme.palette.primary,
    display: "inline",
    width: "2em"
  }
}))(
  ({classes}) => <img alt="logo" className={classes.logo} src={logo}/>
)

export default withStyles({
  ohhey: {
    flexGrow: 1,
    marginLeft: '0.5em',
    fontFamily: 'Lobster'
  }
})(withAuth(({authData: {currentUser, logIn, logOut}, classes}) => (
  <header>
    <AppBar color="default" position="static">
      <Toolbar >
        <MenuButton menuItems={[
          (currentUser && (
            <MenuItem key="/">
              <Link to="/what">What?</Link>
            </MenuItem>
          )),
          <MenuItem key="/featured">
            <Link to="/featured">Featured</Link>
          </MenuItem>
        ]}>
          <HeaderLogo/>
        </MenuButton>
        <Typography variant="h6" className={classes.ohhey}>
          <Link to="/">oh hey!</Link>
        </Typography>
      {currentUser && (
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
      )/* : (
        <Form onSubmit={logIn}>
          <TextInput field="username"/>
          <TextInput field="password" type="password"/>
          <Button type="submit">log in</Button>
        </Form>
      )*/}
      </Toolbar>
    </AppBar>
  </header>
)))
