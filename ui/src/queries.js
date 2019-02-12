import gql from 'graphql-tag';

export const Claims = gql`
query Claims {
  claims {
    id
    body
    supportCount
    opposeCount
    agreeCount
    disagreeCount
  }
}
`

export const EvidenceForClaim = gql`
query EvidencForClaim($claimID: ID) {
  evidenceForClaim(claimID: $claimID) {
    id
    supports
    relevance
    claim {
      id
      body
      supportCount
      opposeCount
      agreeCount
      disagreeCount
    }
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
