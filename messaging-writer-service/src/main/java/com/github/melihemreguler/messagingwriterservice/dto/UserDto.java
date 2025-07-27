package com.github.melihemreguler.messagingwriterservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    private String email;
    
    private String passwordHash;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
