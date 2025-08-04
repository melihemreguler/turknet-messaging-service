package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLogDto, String> {
    
    @Query(value = "{'userId': ?0}", fields = "{'logs': {$slice: [?1, ?2]}}")
    Optional<ActivityLogDto> findByUserIdWithPagination(String userId, int offset, int limit);
    
    @Aggregation(pipeline = {
        "{ $match: { 'userId': ?0 } }",
        "{ $project: { 'totalLogs': { $size: '$logs' } } }"
    })
    Optional<Integer> countLogsByUserId(String userId);
}
