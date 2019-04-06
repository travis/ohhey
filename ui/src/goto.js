export const iBelieve = (history, {slug}, method='replace') => history[method](`/ibelieve/${slug}`)
export const iDontBelieve = (history, {slug}, method='replace') => history[method](`/idontbelieve/${slug}`)
export const someSay = (history, {slug}, method='replace') => history[method](`/somesay/${slug}`)
export const claim = (history, claim, method='replace') => {
  if (claim.myAgreement === -100) {
    iBelieve(history, claim, 'push')
  } else if (claim.myAgreement === 100) {
    iDontBelieve(history, claim, 'push')
  } else {
    someSay(history, claim, 'push')
  }
}
