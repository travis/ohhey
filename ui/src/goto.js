import * as urls from './urls'

export const iBelieve = (history, {slug}, method='replace') => history[method](urls.iBelieve(slug))
export const iDontBelieve = (history, {slug}, method='replace') => history[method](urls.iDontBelieve(slug))
export const someSay = (history, {slug}, method='replace') => history[method](urls.someSay(slug))
export const claim = (history, claim, method='replace') => {
  if (claim.myAgreement === 100) {
    iBelieve(history, claim, 'push')
  } else if (claim.myAgreement === -100) {
    iDontBelieve(history, claim, 'push')
  } else {
    someSay(history, claim, 'push')
  }
}

export const userView = (history, user, claim, method='replace') =>
  history[method](urls.userView(user, claim))
