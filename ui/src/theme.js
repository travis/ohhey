import React from 'react';

import { createMuiTheme } from '@material-ui/core/styles';
import { ThemeProvider as MuiThemeProvider } from '@material-ui/styles';

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
  primary: primaryColor,
  secondary: secondaryColor,
  text: {
    primary: "rgb(0, 0, 0, 0.8)"
  }
};

export const theme = createMuiTheme({
  spacing: 8,
  palette,
  typography: {
    claimBody: "EB Garamond"
  }
})

export const ThemeProvider = ({children}) => (
  <MuiThemeProvider theme={theme}>
    {children}
  </MuiThemeProvider>
)
