// NEVER EDIT THIS FILE MANUALLY
// it should always be updated from ../../../api/resources/schema.gql
export default `
scalar Instant

interface Node {
  id: ID!
}

type User {
  id: ID
  username: String
  email: String
}

input ClaimInput {
  id: ID
  body: String
}

type Claim implements Node {
  id: ID!
  slug: String
  body: String
  createdAt: Instant
  creator: User
  contributors: [User]
  evidence: EvidenceConnection
  supportCount: Int
  opposeCount: Int
  agreement: Int
  agreementCount: Int
  myAgreement: Int
  score: Int
}

type UserClaim implements Node {
  id: ID!
  body: String
  slug: String
  creator: User
  contributors: [User]
  agreement: Int
}

type EvidenceConnection {
  edges: [Evidence]
}

type Evidence {
  id: ID!
  cursor: String
  supports: Boolean
  claim: Claim
  relevance: Float
  myRelevanceRating: Int
  parentClaim: Claim
}

type UserEvidence {
  id: ID!
  cursor: String
  supports: Boolean
  claim: UserClaim
  relevance: Float
}

union SearchResultTarget = Claim | User

type SearchResult {
  score: Float
  result: SearchResultTarget
}

type SearchResults {
  totalCount: Int
  results: [SearchResult]
}

type Query {
  currentUser: User
  claims: [Claim]
  searchClaims(term: String): SearchResults
  suggestClaims(term: String): SearchResults
  claim(slug: String): Claim
  evidenceForClaim(claimID: ID): [Evidence]
  userClaim(username: String!, slug: String!): UserClaim
  userEvidenceForClaim(username: String!, claimID: ID!): [UserEvidence]
}

type Mutation {
  addClaim(claim: ClaimInput): Claim
  addEvidence(claimID: ID!, supports: Boolean!, claim: ClaimInput!): Evidence
  voteOnClaim(claimID: ID!, agreement: Int!): Claim
  voteOnEvidence(evidenceID: ID!, rating: Int!): Evidence
  logIn(username: String, password: String!): User
  logOut: Boolean
}

schema {
  query: Query
  mutation: Mutation
}`
