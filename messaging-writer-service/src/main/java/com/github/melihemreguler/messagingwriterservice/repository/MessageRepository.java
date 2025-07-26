package com.github.melihemreguler.messagingwriterservice.repository;

import com.github.melihemreguler.messagingwriterservice.dto.MessageDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageDto, String> {
    
    List<MessageDto> findByThreadIdOrderByTimestampAsc(String threadId);
    
    List<MessageDto> findBySenderOrRecipientOrderByTimestampDesc(String sender, String recipient);
}
