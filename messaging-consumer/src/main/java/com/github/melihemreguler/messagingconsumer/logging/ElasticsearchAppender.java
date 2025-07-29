package com.github.melihemreguler.messagingconsumer.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElasticsearchAppender extends AppenderBase<ILoggingEvent> {
    
    private static final Logger LOGGER = Logger.getLogger(ElasticsearchAppender.class.getName());
    
    private String elasticsearchUrl;
    private String indexName;
    private WebClient webClient;

    @Override
    public void start() {
        super.start();
        this.webClient = WebClient.builder()
            .baseUrl(elasticsearchUrl)
            .build();
    }
    
    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            Map<String, Object> loggingEvent = createLogDocument(eventObject);
            
            webClient.post()
                .uri("/{indexName}/_doc", indexName)
                .bodyValue(loggingEvent)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(exception -> {
                    LOGGER.log(Level.WARNING, "Unable to send log to Elasticsearch: " + exception.getMessage());
                    return Mono.empty();
                })
                .subscribe();
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating log document for Elasticsearch", e);
        }
    }
    
    private Map<String, Object> createLogDocument(ILoggingEvent event) {
        return LogDocumentBuilder.create()
                .fromEvent(event, "messaging-consumer")
                .build();
    }
    
    // Setters for configuration
    public void setElasticsearchUrl(String elasticsearchUrl) {
        this.elasticsearchUrl = elasticsearchUrl;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
