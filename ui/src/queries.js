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
  creator { username }
  supportCount
  opposeCount
  agreeCount
  disagreeCount
  agree
  disagree
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

export const EvidenceForClaim = gql`
${fullEvidenceFieldsFragment}
query EvidencForClaim($claimID: ID) {
  evidenceForClaim(claimID: $claimID) {
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
    }
  }
`

export const VoteOnClaim = gql`
  ${fullClaimFieldsFragment}
  mutation VoteOnClaim($claimID: ID!, $agree: Boolean!) {
    voteOnClaim(claimID: $claimID, agree: $agree) {
      ...fullClaimFields
    }
  }
`

export const VoteOnEvidence = gql`
  ${fullEvidenceFieldsFragment}
  mutation VoteOnEvidence($evidenceID: ID!, $rating: Int!) {
    voteOnEvidence(evidenceID: $evidenceID, rating: $rating) {
      ...fullEvidenceFields
    }
  }
`
