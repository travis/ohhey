import React from 'react';

import { createMuiTheme } from '@material-ui/core/styles';
import { MuiThemeProvider } from '@material-ui/core/styles';
import { ThemeProvider as StyledComponentsThemeProvider } from 'styled-components';
import { transparentize } from 'polished';

import primaryColor from '@material-ui/core/colors/pink';
import secondaryColor from '@material-ui/core/colors/lightBlue';

//export primaryColor; = '#FF4080'
//export secondaryColor; = '#4EA8FF'

export const fonts = "Acme|Amatic+SC|B612|Cairo|Caveat|Cinzel|Comfortaa|Dancing+Script|EB+Garamond|Gloria+Hallelujah|Gochi+Hand|Laila|Libre+Baskerville|Lobster|Lora|Mali|Noto+Sans|Nova+Script|PT+Sans|Pacifico|Permanent+Marker|Play|Rokkitt|Satisfy|Shadows+Into+Light|Signika|Source+Sans+Pro|Srisakdi".replace(/\+/g, ' ').split("|")

export const randomFont = () => fonts[Math.floor(Math.random() * fonts.length)]

//export const font = randomFont()
export const font = "Roboto"

//export const claimBodyFont = randomFont()
export const claimBodyFont = "EB Garamond"


export const palette = {
  primary: primaryColor,//{ main: primaryColor },
  secondary: secondaryColor,//{ main: secondaryColor },
  claimBodyText: transparentize(0.25, '#000'),
  text: {
    primary: "rgb(0, 0, 0, 0.8)"
  }
};

const muiTheme = createMuiTheme({
  typography: {
    useNextVariants: true,
    fontFamily: font
  },
  palette
})

export const theme = console.log("mui", muiTheme) || {
  ...muiTheme,
  fonts: {
    claimBody: claimBodyFont
  },
  colors: {
    primary: muiTheme.palette.primary.main,
    claimText: muiTheme.palette.text.primary
  }
}

export const ThemeProvider = ({children}) => (
  <MuiThemeProvider theme={theme}>
    <StyledComponentsThemeProvider theme={theme}>
      {children}
    </StyledComponentsThemeProvider>
  </MuiThemeProvider>
)
