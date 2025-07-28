package com.github.melihemreguler.messagingconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MessagingConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingConsumerApplication.class, args);
    }

}
