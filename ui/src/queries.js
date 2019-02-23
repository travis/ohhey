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
  body
  slug
  creator { username }
  supportCount
  opposeCount
  agreement
  agreementCount
  myAgreement
  score
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

export const Claims = gql`
${fullClaimFieldsFragment}
query Claims {
  claims {
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
query EvidenceForClaim($claimID: ID) {
  evidenceForClaim(claimID: $claimID) {
    ...fullEvidenceFields
  }
}
`

export const UserClaim = gql`
${fullClaimFieldsFragment}
query UserClaim($username: String!, $slug: String!) {
  userClaim(username: $username, slug: $slug) {
    ...fullClaimFields
  }
}
`

export const UserEvidenceForClaim = gql`
${fullEvidenceFieldsFragment}
query UserEvidenceForClaim($username: String!, $claimID: ID!) {
  userEvidenceForClaim(username: $username, claimID: $claimID) {
    ...fullEvidenceFields
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
