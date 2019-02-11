import gql from 'graphql-tag';

export const Claims = gql`
query Claims {
  claims {
    body
  }
}
`
