package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository
        extends MongoRepository<MessageDto, String>, MessageRepositoryCustom {

    Page<MessageDto> findByThreadIdOrderByTimestampDesc(String threadId, Pageable pageable);

    long countByThreadId(String threadId);
}
