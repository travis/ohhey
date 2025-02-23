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

type UserClaimMeta {
  id: ID!
  user: User
  agreement: Int
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
  score: Int,
  userMeta(username: String!): UserClaimMeta
  sources: [Source]
}

type Book {
  url: String
  title: String
  author: String
  lccn: String
}

type Source {
  url: String
  title: String
  lccn: String
  page: Int
  book: Book
}

type EvidenceConnection {
  edges: [Evidence]
}

type UserEvidenceMeta {
  id: ID!
  user: User
  relevance: Float
}

type Evidence {
  id: ID!
  cursor: String
  supports: Boolean
  claim: Claim
  relevance: Float
  myRelevanceRating: Int
  parentClaim: Claim
  userMeta(username: String!): UserEvidenceMeta
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
  healthy: Boolean
  currentUser: User
  claims(featured: Boolean): [Claim]
  searchClaims(term: String): SearchResults
  suggestClaims(term: String): SearchResults
  claim(slug: String): Claim
  evidenceForClaim(claimID: ID!, username: String): [Evidence]
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
