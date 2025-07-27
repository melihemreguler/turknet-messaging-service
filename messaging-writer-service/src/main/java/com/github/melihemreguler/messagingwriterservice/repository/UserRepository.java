package com.github.melihemreguler.messagingwriterservice.repository;

import com.github.melihemreguler.messagingwriterservice.dto.UserDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDto, String> {
    
    Optional<UserDto> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    Optional<UserDto> findByEmail(String email);
}
