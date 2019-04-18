import * as urls from './urls'

export const iBelieve = (history, {slug}, method='push') => history[method](urls.iBelieve(slug))
export const iDontBelieve = (history, {slug}, method='push') => history[method](urls.iDontBelieve(slug))
export const someSay = (history, {slug}, method='push') => history[method](urls.someSay(slug))
export const claim = (history, claim, method='push') => {
  if (claim.myAgreement === 100) {
    iBelieve(history, claim, method)
  } else if (claim.myAgreement === -100) {
    iDontBelieve(history, claim, method)
  } else {
    someSay(history, claim, method)
  }
}

export const userView = (history, claim, method='push') =>
  history[method](urls.userView(claim))
