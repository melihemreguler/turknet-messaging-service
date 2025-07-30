const { mergeResolvers } = require('@graphql-tools/merge');
const authResolvers = require('./authResolvers');
const messageResolvers = require('./messageResolvers');
const activityResolvers = require('./activityResolvers');
const healthResolvers = require('./healthResolvers');

const resolvers = mergeResolvers([
  authResolvers,
  messageResolvers,
  activityResolvers,
  healthResolvers
]);

module.exports = resolvers;
