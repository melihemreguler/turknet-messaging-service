package com.github.melihemreguler.messagingconsumer.repository;

import com.github.melihemreguler.messagingconsumer.dto.UserDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDto, String> {}
