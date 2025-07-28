package com.github.melihemreguler.turknetmessagingservice.model.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    
    private List<T> data;
    private long total;
    private int limit;
    private int offset;
    
    public static <T> PaginatedResponse<T> of(List<T> data, long total, int limit, int offset) {
        return new PaginatedResponse<>(data, total, limit, offset);
    }
}
