export const claim = {body: "This is a claim", slug: "this-is-a-claim", score: 100, creator: {username: "travis"}}
export const longClaim = {body: "This is a claim that stretches the limits of this system. It has precisely 255 characters, the maximum number of characters that a claim can have and still be valid. Anything longer than this will be rejected by the system because it is too long. Goodbye.", slug: "this-is-another-claim",
                          score: 8000, creator: {username: "chuchu"}}
export const claims = [
  claim,
  longClaim
].map((claim, i) => ({id: i, ...claim}))
