import React, { Fragment, useState } from 'react'

import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import Drawer from '@material-ui/core/Drawer';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Popover from '@material-ui/core/Popover';
import Divider from '@material-ui/core/Divider';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Grid from '@material-ui/core/Grid';
import Tooltip from '@material-ui/core/Tooltip';

import { Link as RouterLink } from 'react-router-dom'
import MuiLink from '@material-ui/core/Link';

export const Link = (props) => <MuiLink component={RouterLink} {...props}/>

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

export {
  Grid, Paper, Typography, Button, IconButton, Drawer,
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  Popover, Divider, List, ListItem, ListItemText, Tooltip
}
