package com.github.melihemreguler.messagingwriterservice.repository;

import com.github.melihemreguler.messagingwriterservice.dto.SessionDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<SessionDto, String> {
    
    Optional<SessionDto> findByHashedSessionToken(String hashedSessionToken);
    
    List<SessionDto> findByUserId(String userId);
}
