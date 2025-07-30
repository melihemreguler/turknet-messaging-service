const winston = require('winston');
const { ElasticsearchTransport } = require('winston-elasticsearch');

const createLogger = () => {
  const logLevel = process.env.LOG_LEVEL || 'info';
  const logFormat = process.env.LOG_FORMAT || 'json';
  const elasticsearchUrl = process.env.ELASTICSEARCH_URL;
  const elasticsearchEnabled = process.env.ELASTICSEARCH_ENABLED === 'true';

  const format = logFormat === 'json' 
    ? winston.format.combine(
        winston.format.timestamp(),
        winston.format.errors({ stack: true }),
        winston.format.json()
      )
    : winston.format.combine(
        winston.format.timestamp(),
        winston.format.errors({ stack: true }),
        winston.format.simple()
      );

  const transports = [
    new winston.transports.Console({
      handleExceptions: true,
      handleRejections: true
    })
  ];

  if (elasticsearchEnabled && elasticsearchUrl) {
    try {
      const elasticsearchTransport = new ElasticsearchTransport({
        clientOpts: {
          node: elasticsearchUrl,
          maxRetries: 3,
          requestTimeout: 10000,
          sniffOnStart: false
        },
        index: 'turknet-messaging-graphql-logs',
        indexTemplate: {
          name: 'turknet-messaging-graphql-logs-template',
          pattern: 'turknet-messaging-graphql-logs-*',
          settings: {
            number_of_shards: 1,
            number_of_replicas: 0,
            'index.lifecycle.name': 'turknet-messaging-logs-policy',
            'index.lifecycle.rollover_alias': 'turknet-messaging-graphql-logs'
          },
          mappings: {
            properties: {
              '@timestamp': { type: 'date' },
              level: { type: 'keyword' },
              message: { type: 'text' },
              service: { type: 'keyword' },
              userId: { type: 'keyword' },
              sessionId: { type: 'keyword' },
              operation: { type: 'keyword' },
              duration: { type: 'integer' },
              statusCode: { type: 'integer' },
              ip: { type: 'ip' },
              userAgent: { type: 'text' },
              url: { type: 'keyword' },
              method: { type: 'keyword' }
            }
          }
        },
        flushInterval: 2000,
        waitForActiveShards: 1,
        handleExceptions: false,
        handleRejections: false
      });

      transports.push(elasticsearchTransport);
      
      console.log(`Elasticsearch logging enabled: ${elasticsearchUrl}`);
    } catch (error) {
      console.warn(`Failed to initialize Elasticsearch transport: ${error.message}`);
    }
  } else {
    console.log('Elasticsearch logging disabled');
  }

  return winston.createLogger({
    level: logLevel,
    format,
    defaultMeta: { 
      service: 'turknet-messaging-graphql-middleware',
      environment: process.env.NODE_ENV || 'development'
    },
    transports,
    exitOnError: false
  });
};

module.exports = createLogger();
