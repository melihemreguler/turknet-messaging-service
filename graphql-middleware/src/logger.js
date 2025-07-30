const winston = require('winston');

const createLogger = () => {
  const logLevel = process.env.LOG_LEVEL || 'info';
  const logFormat = process.env.LOG_FORMAT || 'json';

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

  return winston.createLogger({
    level: logLevel,
    format,
    defaultMeta: { service: 'turknet-messaging-graphql-middleware' },
    transports: [
      new winston.transports.Console({
        handleExceptions: true,
        handleRejections: true
      })
    ],
    exitOnError: false
  });
};

module.exports = createLogger();
