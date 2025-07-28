package com.github.melihemreguler.messagingconsumer.repository;

import com.github.melihemreguler.messagingconsumer.dto.MessageDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageDto, String> {}
