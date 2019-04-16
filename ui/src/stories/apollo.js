import { ApolloClient } from 'apollo-client';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { SchemaLink } from 'apollo-link-schema';
import { makeExecutableSchema } from 'graphql-tools'
import typeDefs from './typeDefs'
import { claim, claims, supportingEvidence, opposingEvidence } from './data'

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

const link = new SchemaLink({ schema: executableSchema });

export const client = new ApolloClient({
  link,
  cache
});
