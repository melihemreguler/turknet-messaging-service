package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageDto, String> {
    
    List<MessageDto> findByThreadIdOrderByTimestampAsc(String threadId);
    
    List<MessageDto> findBySenderOrderByTimestampDesc(String sender);
    
    List<MessageDto> findByThreadIdAndSenderOrderByTimestampAsc(String threadId, String sender);
}
