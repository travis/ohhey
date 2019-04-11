import { ApolloClient } from 'apollo-client';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { SchemaLink } from 'apollo-link-schema';
import { makeExecutableSchema } from 'graphql-tools'
import typeDefs from './typeDefs'

const claims = [
  {body: "This is a claim", slug: "this-is-a-claim", creator: {username: "travis"}},
  {body: "This is a claim that stretches the limits of this system. It has precisely 255 characters, the maximum number of characters that a claim can have and still be valid. Anything longer than this will be rejected by the system because it is too long. Goodbye.", slug: "this-is-another-claim",
   creator: {username: "chuchu"}}
].map((claim, i) => ({id: i, ...claim}))

const cache = new InMemoryCache();

const resolvers = {
  Query: {
    claims: () => claims
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
