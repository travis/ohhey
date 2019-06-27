import React from 'react';
import {Box} from '../components/ui'
import {Typography} from '../components/ui'
import Claims from '../components/Claims'
import Footer from '../components/Footer'

export default (props) => (
  <Box>
    <Typography variant="h4" margin={2} textAlign="center" fontFamily="claimBody">some people say...</Typography>
    <Claims featured {...props}/>
    <Footer/>
  </Box>
)
