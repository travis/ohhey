export const iBelieve = (slug) =>
  `/ibelieve/${slug}`
export const iDontBelieve = (slug) =>
  `/idontbelieve/${slug}`
export const someSay = (slug) =>
  `/somesay/${slug}`

export const claim = ({slug, myAgreement}) => {
  if (myAgreement === 100) {
    return iBelieve(slug)
  } else if (myAgreement === -100) {
    return iDontBelieve(slug)
  } else {
    return someSay(slug)
  }
}

export const userPrefix = 'someonenamed'
export const believes = (username, slug) =>
  `/${userPrefix}/${username}/believes/${slug}`
export const doesntBelieve = (username, slug) =>
  `/${userPrefix}/${username}/doesntbelieve/${slug}`
export const isntSureIf = (username, slug) =>
  `/${userPrefix}/${username}/isntsureif/${slug}`

export const userView = ({username}, {slug, myAgreement}) => {
  if (myAgreement === 100) {
    return believes(username, slug)
  } else if (myAgreement === -100) {
    return doesntBelieve(username, slug)
  } else {
    return isntSureIf(username, slug)
  }
}
