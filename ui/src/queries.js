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
