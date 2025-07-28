package com.github.melihemreguler.messagingconsumer.repository;

import com.github.melihemreguler.messagingconsumer.dto.ActivityLogDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLogDto, String> {
    Optional<ActivityLogDto> findByUserId(String userId);
}
