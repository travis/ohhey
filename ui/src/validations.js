export const claimBody = (body) => {
  if (body && body.length > 255) {
    return 'You only have 255 characters to work with, time to do some editing!'
  }
}
