import { ApolloClient } from 'apollo-client';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { SchemaLink } from 'apollo-link-schema';
import { makeExecutableSchema } from 'graphql-tools'
import typeDefs from './typeDefs'
import { claim, claims, supportingEvidence, opposingEvidence } from './data'
import { makeLinkFrom } from '../clients'

const cache = new InMemoryCache();

const resolvers = {
  Query: {
    claim: () => claim,
    claims: () => claims,
    evidenceForClaim: () => [supportingEvidence, opposingEvidence]
  }
}

const executableSchema = makeExecutableSchema({
  typeDefs,
  resolvers,
  resolverValidationOptions: {
    requireResolversForResolveType: false
  }
});

export const client = new ApolloClient({
  link: makeLinkFrom(new SchemaLink({ schema: executableSchema })),
  cache
});
