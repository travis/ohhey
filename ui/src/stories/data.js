export const claim = {
  id: 1, body: "This is a claim", slug: "this-is-a-claim", score: 100, creator: {username: "travis"},
  userMeta: {agreement: 100}
}
export const longClaim = {id: 2, body: "This is a claim that stretches the limits of this system. It has precisely 255 characters, the maximum number of characters that a claim can have and still be valid. Anything longer than this will be rejected by the system because it is too long. Goodbye.", slug: "this-is-another-claim",
                          score: 8000, creator: {username: "chuchu"}}

export const supportingClaim = {
  id: 3, body: "It exists in ohhey", slug: "it-exists-in-ohhey", score: 0,
  creator: {username: "travis"},
  userMeta: {agreement: 100}
}
export const supportingEvidence = {id: 100, claim: supportingClaim, supports: true}

export const opposingClaim = {
  id: 4, body: "'This is a claim' is a tautology", slug: "this-is-a-claim-is-a-tautology", score: 100,
  creator: {username: "james"},
  userMeta: {agreement: -100}
}
export const opposingEvidence = {id: 101, claim: opposingClaim, supports: false, userMeta: {relevance: 45}}

export const claims = [
  claim,
  longClaim
].map((claim, i) => ({id: i, ...claim}))
