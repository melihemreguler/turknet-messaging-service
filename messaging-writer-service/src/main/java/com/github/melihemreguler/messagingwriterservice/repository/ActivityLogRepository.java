package com.github.melihemreguler.messagingwriterservice.repository;

import com.github.melihemreguler.messagingwriterservice.dto.ActivityLogDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLogDto, String> {
    
    List<ActivityLogDto> findByUserIdOrderByTimestampDesc(String userId);
    
    List<ActivityLogDto> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    List<ActivityLogDto> findBySuccessfulFalseOrderByTimestampDesc();
}
