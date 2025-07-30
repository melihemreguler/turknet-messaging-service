const request = require('supertest');
const GraphQLServer = require('../src/server');

describe('GraphQL Server', () => {
  let app;
  let server;

  beforeAll(async () => {
    // Set test environment
    process.env.NODE_ENV = 'test';
    process.env.PORT = '0'; // Use random port
    process.env.GRAPHQL_PLAYGROUND = 'false';
    process.env.GRAPHQL_INTROSPECTION = 'true';
    process.env.REST_API_BASE_URL = 'http://localhost:8080';
    
    const graphqlServer = new GraphQLServer();
    server = await graphqlServer.start();
    app = graphqlServer.app;
  });

  afterAll(async () => {
    if (server) {
      server.close();
    }
  });

  describe('Health endpoints', () => {
    test('GET /health should return health status', async () => {
      const response = await request(app)
        .get('/health')
        .expect(200);

      expect(response.body).toMatchObject({
        status: 'UP',
        service: 'turknet-messaging-graphql-middleware'
      });
      expect(response.body.timestamp).toBeDefined();
    });

    test('GET /ready should return readiness status', async () => {
      const response = await request(app)
        .get('/ready')
        .expect(200);

      expect(response.body).toMatchObject({
        status: 'READY',
        service: 'turknet-messaging-graphql-middleware'
      });
      expect(response.body.timestamp).toBeDefined();
    });
  });

  describe('GraphQL endpoint', () => {
    test('POST /graphql should handle introspection query', async () => {
      const introspectionQuery = `
        query {
          __schema {
            types {
              name
            }
          }
        }
      `;

      const response = await request(app)
        .post('/graphql')
        .send({ query: introspectionQuery })
        .expect(200);

      expect(response.body.data).toBeDefined();
      expect(response.body.data.__schema).toBeDefined();
      expect(response.body.data.__schema.types).toBeInstanceOf(Array);
    });

    test('POST /graphql should handle health query', async () => {
      const healthQuery = `
        query {
          health {
            status
            timestamp
          }
        }
      `;

      const response = await request(app)
        .post('/graphql')
        .send({ query: healthQuery })
        .expect(200);

      expect(response.body.data).toBeDefined();
      expect(response.body.data.health).toMatchObject({
        status: expect.any(String),
        timestamp: expect.any(String)
      });
    });

    test('POST /graphql should return error for invalid query', async () => {
      const invalidQuery = `
        query {
          invalidField
        }
      `;

      const response = await request(app)
        .post('/graphql')
        .send({ query: invalidQuery })
        .expect(200);

      expect(response.body.errors).toBeDefined();
      expect(response.body.errors).toBeInstanceOf(Array);
      expect(response.body.errors.length).toBeGreaterThan(0);
    });
  });

  describe('Error handling', () => {
    test('GET /nonexistent should return 404', async () => {
      const response = await request(app)
        .get('/nonexistent')
        .expect(404);

      expect(response.body).toMatchObject({
        error: 'Not Found',
        message: 'The requested endpoint does not exist'
      });
    });
  });
});
