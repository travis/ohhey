import React, { Fragment, useState } from 'react';
import { graphql, compose } from "react-apollo";
import { withRouter } from "react-router-dom";
import {
  Paper, Typography, Button, Drawer, ExpansionPanel, ExpansionPanelSummary, ExpansionPanelDetails,
  Link
} from './ui'


export default ({username, claim: {slug, body}}) => (
  <Paper>
    <Typography variant="h5" align="center">
      @{username} believes
    </Typography>
    <Typography variant="h4" color="textPrimary" align="center">
      <Link to={`/someonenamed/${username}/believes/${slug}`}>{body}</Link>
    </Typography>
  </Paper>
)
