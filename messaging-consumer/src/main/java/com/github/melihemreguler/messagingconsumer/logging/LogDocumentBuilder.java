package com.github.melihemreguler.messagingconsumer.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class LogDocumentBuilder {
    
    private static final DateTimeFormatter ISO_8601_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .withZone(ZoneId.systemDefault());
    
    private final Map<String, Object> document;
    
    private LogDocumentBuilder() {
        this.document = new LinkedHashMap<>();
    }
    
    public static LogDocumentBuilder create() {
        return new LogDocumentBuilder();
    }
    
    public LogDocumentBuilder withTimestamp(long timestampMillis) {
        document.put("@timestamp", ISO_8601_FORMAT.format(Instant.ofEpochMilli(timestampMillis)));
        return this;
    }
    
    public LogDocumentBuilder withLevel(String level) {
        document.put("level", level);
        return this;
    }
    
    public LogDocumentBuilder withMessage(String message) {
        document.put("message", message);
        return this;
    }
    
    public LogDocumentBuilder withLoggerName(String loggerName) {
        document.put("logger_name", loggerName);
        return this;
    }
    
    public LogDocumentBuilder withThreadName(String threadName) {
        document.put("thread_name", threadName);
        return this;
    }
    
    public LogDocumentBuilder withApplication(String application) {
        document.put("application", application);
        return this;
    }
    
    public LogDocumentBuilder withService(String service) {
        document.put("service", service);
        return this;
    }
    
    public LogDocumentBuilder withMdc(Map<String, String> mdcProperties) {
        if (mdcProperties != null && !mdcProperties.isEmpty()) {
            document.put("mdc", mdcProperties);
        }
        return this;
    }
    
    public LogDocumentBuilder withException(IThrowableProxy throwableProxy) {
        if (throwableProxy != null) {
            Map<String, Object> exception = new LinkedHashMap<>();
            exception.put("class", throwableProxy.getClassName());
            exception.put("message", throwableProxy.getMessage());
            exception.put("stack_trace", throwableProxy.getStackTraceElementProxyArray());
            document.put("exception", exception);
        }
        return this;
    }
    
    public LogDocumentBuilder withCustomField(String key, Object value) {
        document.put(key, value);
        return this;
    }
    
    public LogDocumentBuilder fromEvent(ILoggingEvent event, String serviceName) {
        return this.withTimestamp(event.getTimeStamp())
                   .withLevel(event.getLevel().toString())
                   .withMessage(event.getFormattedMessage())
                   .withLoggerName(event.getLoggerName())
                   .withThreadName(event.getThreadName())
                   .withApplication("turknet-messaging-service")
                   .withService(serviceName)
                   .withMdc(event.getMDCPropertyMap())
                   .withException(event.getThrowableProxy());
    }
    
    public Map<String, Object> build() {
        return new LinkedHashMap<>(document);
    }
}
