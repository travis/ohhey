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
