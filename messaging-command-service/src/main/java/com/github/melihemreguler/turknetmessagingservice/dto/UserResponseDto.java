package com.github.melihemreguler.turknetmessagingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private String id;
    private String username;
    private LocalDateTime createdAt;
    
    public static UserResponseDto fromUserDto(UserDto userDto) {
        return new UserResponseDto(
            userDto.getId(),
            userDto.getUsername(),
            userDto.getCreatedAt()
        );
    }
}
