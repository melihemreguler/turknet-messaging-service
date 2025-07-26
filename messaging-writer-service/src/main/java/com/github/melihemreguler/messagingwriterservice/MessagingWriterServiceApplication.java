package com.github.melihemreguler.messagingwriterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MessagingWriterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingWriterServiceApplication.class, args);
    }

}
