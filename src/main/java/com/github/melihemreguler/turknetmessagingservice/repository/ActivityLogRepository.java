package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLogDto, String> {
    
    List<ActivityLogDto> findByUsernameOrderByTimestampDesc(String username);
    
    List<ActivityLogDto> findByUsernameAndTimestampBetween(String username, LocalDateTime startTime, LocalDateTime endTime);
    
    List<ActivityLogDto> findByUsernameAndSuccessfulFalse(String username);
}
