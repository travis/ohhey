import { createMuiTheme } from '@material-ui/core/styles';

import indigo from '@material-ui/core/colors/indigo';
import pink from '@material-ui/core/colors/pink';
import yellow from '@material-ui/core/colors/yellow';


const fonts = "Acme|Amatic+SC|B612|Cairo|Caveat|Cinzel|Comfortaa|Dancing+Script|EB+Garamond|Gloria+Hallelujah|Gochi+Hand|Laila|Lora|Mali|Noto+Sans|Nova+Script|PT+Sans|Pacifico|Permanent+Marker|Play|Rokkitt|Satisfy|Shadows+Into+Light|Signika|Source+Sans+Pro|Srisakdi".
      replace(/\+/g, ' ').split("|")

export const randomFont = () => fonts[Math.floor(Math.random() * fonts.length)]

//export const font = randomFont()
export const font = "Source Sans Pro"

export const claimBodyFont = randomFont()
//export const claimBodyFont = "Amatic SC"

const palette = {
  primary: { main: '#FF4081' },
  secondary: { main: '#FFFF00' }
};

export const theme = createMuiTheme({
  typography: {
    useNextVariants: true,
    fontFamily: font
  },
  palette
})
