import React from 'react'

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
import { Link as RouterLink } from 'react-router-dom'
import MuiLink from '@material-ui/core/Link';

export const Link = (props) => <MuiLink component={RouterLink} {...props}/>

export {
  Paper, Typography, Button, IconButton, Drawer,
  ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  Popover, Divider, List, ListItem, ListItemText
}
