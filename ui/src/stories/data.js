export const chuchu = {
  username: "chuchu"
}

export const james = {
  username: "james"
}

export const ian = {
  username: "ian"
}

export const travis = {
  username: "travis"
}

export const toby = {
  username: "toby"
}

export const claim = {
  id: 1, body: "This is a claim", slug: "this-is-a-claim", score: 100, creator: travis,
  userMeta: {id: "1:travis", agreement: 100, user: travis}
}
export const longClaim = {id: 2, body: "This is a claim that stretches the limits of this system. It has precisely 255 characters, the maximum number of characters that a claim can have and still be valid. Anything longer than this will be rejected by the system because it is too long. Goodbye.", slug: "this-is-another-claim",
                          score: 8000, creator: chuchu}

export const supportingClaim = {
  id: 3, body: "It exists in ohhey", slug: "it-exists-in-ohhey", score: 0,
  creator: travis,
  userMeta: {id: "3:toby", agreement: 100, user: toby}
}
export const supportingEvidence = {id: 100, claim: supportingClaim, supports: true}

export const opposingClaim = {
  id: 4, body: "'This is a claim' is a tautology", slug: "this-is-a-claim-is-a-tautology", score: 100,
  creator: james,
  userMeta: {id: "4:toby", agreement: -100, user: toby}
}
export const opposingEvidence = {id: 101, claim: opposingClaim, supports: false,
                                 userMeta: {id: "101:ian", relevance: 45, user: ian}}

export const claims = [
  claim,
  longClaim
].map((claim, i) => ({id: i, ...claim}))
