export const iBelieve = (history, {slug}, method='replace') => history[method](`/ibelieve/${slug}`)
export const iDontBelieve = (history, {slug}, method='replace') => history[method](`/idontbelieve/${slug}`)
export const someSay = (history, {slug}, method='replace') => history[method](`/somesay/${slug}`)
