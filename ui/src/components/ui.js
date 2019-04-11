import React, { Fragment, useState } from 'react'

import MuiPaper from '@material-ui/core/Paper';
import MuiTypography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import Drawer from '@material-ui/core/Drawer';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Popover from '@material-ui/core/Popover';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import Divider from '@material-ui/core/Divider';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Grid from '@material-ui/core/Grid';
import Tooltip from '@material-ui/core/Tooltip';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import { withTheme } from '@material-ui/core/styles';

import { Link as RouterLink } from 'react-router-dom'
import MuiLink from '@material-ui/core/Link';

import SpinkitSpinner from 'react-spinkit'
import styled from 'styled-components'
import { compose, space, width, fontSize, fontFamily, color, backgroundImage } from 'styled-system'


import * as theme from '../theme';

export const Link = (props) => <MuiLink color="inherit" component={RouterLink} {...props}/>

export const PopoverButton = ({ariaID="popover", children, popoverContent}) => {
  const [buttonEl, setShowPopover] = useState(null)
  const popoverShown = Boolean(buttonEl);
  return (
    <Fragment>
    <Button
      aria-owns={popoverShown ? ariaID : undefined}
      aria-haspopup="true"
      onClick={(e) => setShowPopover(e.target)}>
      {children}
    </Button>
      <Popover
        id={ariaID}
        open={popoverShown}
        anchorEl={buttonEl}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center'
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center'
        }}
        onClose={() => setShowPopover(null)}>
        {popoverContent}
      </Popover>
    </Fragment>
  )
}

export const MenuButton = ({ariaID="menu", children, menuItems}) => {
  const [buttonEl, setShowMenu] = useState(null)
  const menuShown = Boolean(buttonEl);
  return (
    <Fragment>
      <Button
        aria-owns={menuShown ? ariaID : undefined}
        aria-haspopup="true"
        onClick={(e) => setShowMenu(e.target)}>
        {children}
      </Button>
      <Menu
        id={ariaID}
        open={menuShown}
        anchorEl={buttonEl}
        onClick={() => setShowMenu(null)}
        onClose={() => setShowMenu(null)}>
        {menuItems}
      </Menu>
    </Fragment>
  )
}

export const Typography = styled(
  // don't pass "color" along to MuiTypography because it means something different there
  ({color, ...props}) => <MuiTypography {...props}/>
)(
  compose(fontFamily)
)

export const Paper = styled(
  ({backgroundImage, ...props}) => <MuiPaper elevation={1} {...props}/>
)(compose(backgroundImage, space))

export const ClaimPaper = (props) => (
  <Paper padding={3}
         backgroundImage="url('https://www.transparenttextures.com/patterns/groovepaper.png')"
         {...props} />
)

export const ClaimBody = (props) => (
  <Typography fontFamily="claimBody" variant="h5" align="center" color="claimText" {...props}/>
)

const StyledSpinner = styled(SpinkitSpinner)(compose(color))

export const Spinner = (props) => (
  <StyledSpinner name="chasing-dots" color="primary" {...props}/>
)

export {
  Grid, Button, IconButton, Drawer,
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  Popover, Divider, List, ListItem, ListItemText, Tooltip,
  Menu, MenuItem, AppBar, Toolbar
}
