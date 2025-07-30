const path = require('path');
require('dotenv').config({
  path: path.join(__dirname, '..', '.env')
});
const GraphQLServer = require('./server');

const server = new GraphQLServer();
server.start().catch((error) => {
  console.error('Failed to start GraphQL server:', error);
  process.exit(1);
});
