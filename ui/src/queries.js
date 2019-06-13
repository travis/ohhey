import gql from 'graphql-tag';

export const CurrentUser = gql`
  query CurrentUser {
    currentUser {
      email
      username
    }
  }
`

const fullClaimFieldsFragment = gql`
fragment fullClaimFields on Claim {
  id
  slug
  body
  createdAt
  creator { username }
  supportCount
  opposeCount
  agreement
  agreementCount
  myAgreement
  score
  sources { url, title, lccn, page, book { url, title, author, lccn } }
}
`

const fullEvidenceFieldsFragment = gql`
${fullClaimFieldsFragment}
fragment fullEvidenceFields on Evidence {
  id
  supports
  relevance
  myRelevanceRating
  claim {
  ...fullClaimFields
  }
}
`

const fullUserClaimFieldsFragment = gql`
fragment fullUserClaimFields on Claim {
  id
  body
  slug
  creator { username }
  myAgreement
  sources { url, title, lccn, page, book { url, title, author, lccn } }
  userMeta(username: $username) {
    id
    agreement
    user {
      username
    }
  }
}
`

const fullUserEvidenceFieldsFragment = gql`
${fullUserClaimFieldsFragment}
fragment fullUserEvidenceFields on Evidence {
  id
  supports
  claim {
  ...fullUserClaimFields
  }
  userMeta(username: $username) { relevance }
}
`

export const Claims = gql`
${fullClaimFieldsFragment}
query Claims($featured: Boolean) {
  claims(featured: $featured) {
    ...fullClaimFields
  }
}
`

export const Claim = gql`
${fullClaimFieldsFragment}
query Claim($slug: String) {
  claim(slug: $slug) {
    ...fullClaimFields
  }
}
`

export const EvidenceForClaim = gql`
${fullEvidenceFieldsFragment}
query EvidenceForClaim($claimID: ID!) {
  evidenceForClaim(claimID: $claimID) {
    ...fullEvidenceFields
  }
}
`

export const UserClaim = gql`
${fullUserClaimFieldsFragment}
query UserClaim($username: String!, $slug: String!) {
  claim(slug: $slug) {
    ...fullUserClaimFields
  }
}
`

export const UserEvidenceForClaim = gql`
${fullUserEvidenceFieldsFragment}
query UserEvidenceForClaim($username: String!, $claimID: ID!) {
  evidenceForClaim(username: $username, claimID: $claimID) {
    ...fullUserEvidenceFields
  }
}
`

export const CommentsQuery = gql`
  query Comments($ref: String!) {
    comments @rtdbQuery(ref: $ref, type: "Comment") @array {
      id @key
      body
    }
  }
`;

export const CreateComment = gql`
  fragment CommentInput on firebase {
    body: String
  }

  mutation($ref: String!, $input: CommentInput!) {
    createComment(input: $input) @rtdbPush(ref: $ref) {
      id @pushKey
      body
    }
  }
`

export const SubscribeToComments = gql`
  subscription($ref: String!) {
    newComment @rtdbSub(ref: $ref, event: "child_added", type: "Comment") {
      id @key
      body
    }
  }
`;

export const AddEvidence = gql`
  ${fullEvidenceFieldsFragment}
  mutation AddEvidence($claimID: ID!, $supports: Boolean!, $claim: ClaimInput!) {
    addEvidence(claimID: $claimID, supports: $supports, claim: $claim) {
      ...fullEvidenceFields
      parentClaim {
        ...fullClaimFields
      }
    }
  }
`

export const AddClaim = gql`
  ${fullClaimFieldsFragment}
  mutation AddClaim($claim: ClaimInput!) {
    addClaim(claim: $claim) {
      ...fullClaimFields
    }
  }
`

export const VoteOnClaim = gql`
  ${fullClaimFieldsFragment}
  mutation VoteOnClaim($claimID: ID!, $agreement: Int!) {
    voteOnClaim(claimID: $claimID, agreement: $agreement) {
      ...fullClaimFields
    }
  }
`

export const VoteOnEvidence = gql`
  ${fullEvidenceFieldsFragment}
  mutation VoteOnEvidence($evidenceID: ID!, $rating: Int!) {
    voteOnEvidence(evidenceID: $evidenceID, rating: $rating) {
      ...fullEvidenceFields
      parentClaim {
        ...fullClaimFields
      }
    }
  }
`

export const SearchClaims = gql`
${fullClaimFieldsFragment}
query SearchClaims($term: String!) {
  searchClaims(term: $term) {
    totalCount
    results {
      score
      result {
        __typename
        ... on Claim {
          ...fullClaimFields
        }
      }
    }
  }
}
`

export const QuickSearchClaims = gql`
${fullClaimFieldsFragment}
query QuickSearchClaims($term: String!) {
  suggestClaims(term: $term) {
    totalCount
    results {
      score
      result {
        __typename
        ... on Claim {
          ...fullClaimFields
        }
      }
    }
  }
}
`

export const LogIn = gql`
  mutation LogIn($username: String, $password: String!) {
    logIn(username: $username, password: $password) {
      username
    }
  }
`

export const LogOut = gql`
  mutation LogOut {
    logOut
  }
`
