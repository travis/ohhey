import React, { Fragment, useState, createRef, forwardRef } from 'react'

import Box from '@material-ui/core/Box';
import BottomNavigation from '@material-ui/core/BottomNavigation';
import BottomNavigationAction from '@material-ui/core/BottomNavigationAction';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import Dialog from '@material-ui/core/Dialog';
import MuiPaper from '@material-ui/core/Paper';
import MuiTypography from '@material-ui/core/Typography';
import MuiButton from '@material-ui/core/Button';
import MuiIconButton from '@material-ui/core/IconButton';
import Drawer from '@material-ui/core/Drawer';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Popover from '@material-ui/core/Popover';
import Popper from '@material-ui/core/Popper';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import Divider from '@material-ui/core/Divider';
import MuiList from '@material-ui/core/List';
import MuiListItem from '@material-ui/core/ListItem';
import MuiListItemText from '@material-ui/core/ListItemText';
import MuiListItemIcon from '@material-ui/core/ListItemIcon';
import MuiGrid from '@material-ui/core/Grid';
import Tooltip from '@material-ui/core/Tooltip';
import AppBar from '@material-ui/core/AppBar';
import MuiToolbar from '@material-ui/core/Toolbar';

import { Link as RouterLink } from 'react-router-dom'
import MuiLink from '@material-ui/core/Link';

import SpinkitSpinner from 'react-spinkit'
import { styled } from '@material-ui/styles';
import { compose, css, positions, palette, spacing, typography, flexbox, sizing } from "@material-ui/system"

const AdapterLink = forwardRef((props, ref) => <RouterLink innerRef={ref} {...props} />);

export const UnstyledLink = ({children, ...props}) => <MuiLink color="inherit" component={AdapterLink} {...props}>{children}</MuiLink>

export const Link = styled(UnstyledLink)(compose(flexbox, typography, spacing))

export const NewTabLink = ({children, ...props}) => <a target="_blank" rel="noopener noreferrer" {...props}>{children}</a>

export const PopoverButton = styled(({ariaID="popover", children, popoverContent, ...buttonProps}) => {
  const [buttonEl, setShowPopover] = useState(null)
  const popoverShown = Boolean(buttonEl);
  return (
    <Fragment>
      <Button
        aria-owns={popoverShown ? ariaID : undefined}
        aria-haspopup="true"
        onClick={(e) => setShowPopover(e.target)}
        {...buttonProps}>
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
})(compose(positions, spacing, sizing, typography))

export const MenuButton = ({ariaID="menu", children, menuItems, ...props}) => {
  const [buttonEl, setShowMenu] = useState(null)
  const menuShown = Boolean(buttonEl);
  const buttonRef = createRef();
  return (
    <Fragment>
      <Button
        aria-owns={menuShown ? ariaID : undefined}
        aria-haspopup="true"
        onClick={(e) => setShowMenu(buttonRef.current)}
        ref={buttonRef}
        {...props}>
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

export const Paper = styled(
  (props) => <MuiPaper elevation={1} {...props}/>
)(compose(spacing))

export const ClaimPaper = styled((props) => (
  <Paper padding={3}
         {...props} />
))({
  backgroundImage: "url('https://www.transparenttextures.com/patterns/groovepaper.png')"
})

export const ClaimBody = (props) => (
  <Typography fontFamily="claimBody" variant="h5" align="center" color="textPrimary" {...props}/>
)

//export const Claim

const StyledSpinner = styled(SpinkitSpinner)(compose(palette))

export const Spinner = (props) => (
  <StyledSpinner name="chasing-dots" color="primary.main" {...props}/>
)

const Typography = styled(MuiTypography)(css(compose(typography, flexbox, spacing)))
const Grid = styled(MuiGrid)(compose(spacing))
const Button = styled(MuiButton)(compose(typography, flexbox))
const List = styled(MuiList)(compose(spacing))
const ListItem = styled(MuiListItem)(compose(spacing))
const ListItemText = styled(MuiListItemText)(compose(spacing))
const ListItemIcon = styled(MuiListItemIcon)(compose(spacing))
const Toolbar = styled(MuiToolbar)(compose(spacing, positions, sizing, flexbox))
const IconButton = styled(MuiIconButton)(css(compose(spacing, positions)))


export {
  Box, BottomNavigation, BottomNavigationAction, Grid, Button, IconButton, Drawer,
  Typography, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  Popover, Divider, List, ListItem, ListItemText, ListItemIcon, Tooltip,
  Menu, MenuItem, AppBar, Toolbar, Dialog, Popper, ClickAwayListener
}
